"""
Рівень 2: API для керування університетською системою (Django ORM).
Рівень 3: Кешування часто використовуваних даних.
Рівень 4: Транзакції для групування операцій.
"""

import json
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.cache import cache_page
from django.core.cache import cache
from django.db import transaction, IntegrityError
from django.db.models import Avg, Count, Q
from .models import Student, Teacher, Course, Lesson, Grade


def _parse_body(request):
    """Допоміжна функція для парсингу JSON з тіла запиту."""
    try:
        return json.loads(request.body)
    except json.JSONDecodeError:
        return {}


# ==================================================================
# Рівень 2: CRUD для студентів
# ==================================================================

@csrf_exempt
def students_list(request):
    """GET — список студентів (з кешуванням), POST — створити студента."""
    if request.method == 'GET':
        # Рівень 3: кешування списку студентів
        cache_key = 'students_list'
        data = cache.get(cache_key)
        if data is None:
            students = Student.objects.prefetch_related('courses').all()
            data = [{
                'id': s.id,
                'student_id': s.student_id,
                'first_name': s.first_name,
                'last_name': s.last_name,
                'email': s.email,
                'courses': [c.code for c in s.courses.all()],
            } for s in students]
            cache.set(cache_key, data, 300)
        return JsonResponse(data, safe=False)

    elif request.method == 'POST':
        body = _parse_body(request)
        try:
            # Рівень 4: транзакція для створення студента з курсами
            with transaction.atomic():
                student = Student.objects.create(
                    first_name=body.get('first_name', ''),
                    last_name=body.get('last_name', ''),
                    email=body.get('email', ''),
                    student_id=body.get('student_id', ''),
                )
                # Якщо передані курси — записати студента
                course_ids = body.get('course_ids', [])
                if course_ids:
                    courses = Course.objects.filter(id__in=course_ids)
                    student.courses.set(courses)

            cache.delete('students_list')  # Інвалідація кешу
            return JsonResponse({'id': student.id, 'message': 'Студента створено'}, status=201)
        except IntegrityError as e:
            return JsonResponse({'error': str(e)}, status=400)

    return JsonResponse({'error': 'Method not allowed'}, status=405)


@csrf_exempt
def student_detail(request, pk):
    """GET — деталі студента, PUT — оновити, DELETE — видалити."""
    try:
        student = Student.objects.prefetch_related('courses', 'grades__course').get(pk=pk)
    except Student.DoesNotExist:
        return JsonResponse({'error': 'Студента не знайдено'}, status=404)

    if request.method == 'GET':
        data = {
            'id': student.id,
            'student_id': student.student_id,
            'first_name': student.first_name,
            'last_name': student.last_name,
            'email': student.email,
            'courses': [{'id': c.id, 'code': c.code, 'name': c.name} for c in student.courses.all()],
            'grades': [{
                'id': g.id, 'course': g.course.code,
                'type': g.get_grade_type_display(), 'score': g.score,
            } for g in student.grades.all()],
        }
        return JsonResponse(data)

    elif request.method == 'PUT':
        body = _parse_body(request)
        # Рівень 4: транзакція для оновлення
        with transaction.atomic():
            if 'first_name' in body: student.first_name = body['first_name']
            if 'last_name' in body: student.last_name = body['last_name']
            if 'email' in body: student.email = body['email']
            student.save()
            if 'course_ids' in body:
                courses = Course.objects.filter(id__in=body['course_ids'])
                student.courses.set(courses)
        cache.delete('students_list')
        return JsonResponse({'message': 'Студента оновлено'})

    elif request.method == 'DELETE':
        # Рівень 4: транзакція для видалення (каскадне)
        with transaction.atomic():
            student.delete()
        cache.delete('students_list')
        return JsonResponse({'message': 'Студента видалено'})

    return JsonResponse({'error': 'Method not allowed'}, status=405)


# ==================================================================
# Рівень 2: CRUD для курсів
# ==================================================================

@csrf_exempt
def courses_list(request):
    """GET — список курсів (кешований), POST — створити курс."""
    if request.method == 'GET':
        # Рівень 3: кешування списку курсів
        cache_key = 'courses_list'
        data = cache.get(cache_key)
        if data is None:
            courses = Course.objects.prefetch_related('teachers', 'students').annotate(
                student_count=Count('students'),
                avg_grade=Avg('grades__score'),
            ).all()
            data = [{
                'id': c.id,
                'code': c.code,
                'name': c.name,
                'credits': c.credits,
                'teachers': [f"{t.last_name} {t.first_name}" for t in c.teachers.all()],
                'student_count': c.student_count,
                'avg_grade': round(c.avg_grade, 1) if c.avg_grade else None,
            } for c in courses]
            cache.set(cache_key, data, 300)
        return JsonResponse(data, safe=False)

    elif request.method == 'POST':
        body = _parse_body(request)
        try:
            with transaction.atomic():
                course = Course.objects.create(
                    name=body.get('name', ''),
                    code=body.get('code', ''),
                    credits=body.get('credits', 3),
                    description=body.get('description', ''),
                )
                teacher_ids = body.get('teacher_ids', [])
                if teacher_ids:
                    teachers = Teacher.objects.filter(id__in=teacher_ids)
                    course.teachers.set(teachers)

            cache.delete('courses_list')
            return JsonResponse({'id': course.id, 'message': 'Курс створено'}, status=201)
        except IntegrityError as e:
            return JsonResponse({'error': str(e)}, status=400)

    return JsonResponse({'error': 'Method not allowed'}, status=405)


@csrf_exempt
def course_detail(request, pk):
    """GET — деталі курсу, DELETE — видалити."""
    try:
        course = Course.objects.prefetch_related('teachers', 'students', 'lessons').get(pk=pk)
    except Course.DoesNotExist:
        return JsonResponse({'error': 'Курс не знайдено'}, status=404)

    if request.method == 'GET':
        data = {
            'id': course.id,
            'code': course.code,
            'name': course.name,
            'credits': course.credits,
            'description': course.description,
            'teachers': [{'id': t.id, 'name': str(t)} for t in course.teachers.all()],
            'students': [{'id': s.id, 'name': str(s)} for s in course.students.all()],
            'lessons': [{'id': l.id, 'topic': l.topic, 'type': l.get_lesson_type_display()}
                        for l in course.lessons.all()[:10]],
        }
        return JsonResponse(data)

    elif request.method == 'DELETE':
        with transaction.atomic():
            course.delete()
        cache.delete('courses_list')
        return JsonResponse({'message': 'Курс видалено'})

    return JsonResponse({'error': 'Method not allowed'}, status=405)


# ==================================================================
# Рівень 2: CRUD для оцінок
# ==================================================================

@csrf_exempt
def grades_list(request):
    """GET — список оцінок (з фільтрацією), POST — додати оцінку."""
    if request.method == 'GET':
        grades = Grade.objects.select_related('student', 'course').all()

        # Фільтрація
        student_id = request.GET.get('student_id')
        course_id = request.GET.get('course_id')
        if student_id:
            grades = grades.filter(student_id=student_id)
        if course_id:
            grades = grades.filter(course_id=course_id)

        data = [{
            'id': g.id,
            'student': str(g.student),
            'course': g.course.code,
            'type': g.get_grade_type_display(),
            'score': g.score,
            'date': str(g.date),
        } for g in grades[:50]]
        return JsonResponse(data, safe=False)

    elif request.method == 'POST':
        body = _parse_body(request)
        try:
            # Рівень 4: транзакція для додавання оцінки
            with transaction.atomic():
                student = Student.objects.get(id=body['student_id'])
                course = Course.objects.get(id=body['course_id'])

                # Перевірка: студент записаний на курс
                if not student.courses.filter(id=course.id).exists():
                    return JsonResponse(
                        {'error': 'Студент не записаний на цей курс'}, status=400
                    )

                grade = Grade.objects.create(
                    student=student,
                    course=course,
                    grade_type=body.get('grade_type', 'homework'),
                    score=body.get('score', 0),
                    comment=body.get('comment', ''),
                )
            cache.delete('courses_list')  # Оновити середній бал
            return JsonResponse({'id': grade.id, 'message': 'Оцінку додано'}, status=201)
        except (Student.DoesNotExist, Course.DoesNotExist):
            return JsonResponse({'error': 'Студент або курс не знайдено'}, status=404)
        except (KeyError, IntegrityError) as e:
            return JsonResponse({'error': str(e)}, status=400)

    return JsonResponse({'error': 'Method not allowed'}, status=405)


# ==================================================================
# Рівень 2: CRUD для викладачів
# ==================================================================

@csrf_exempt
def teachers_list(request):
    """GET — список викладачів, POST — створити."""
    if request.method == 'GET':
        teachers = Teacher.objects.prefetch_related('courses').all()
        data = [{
            'id': t.id,
            'first_name': t.first_name,
            'last_name': t.last_name,
            'email': t.email,
            'department': t.department,
            'courses': [c.code for c in t.courses.all()],
        } for t in teachers]
        return JsonResponse(data, safe=False)

    elif request.method == 'POST':
        body = _parse_body(request)
        try:
            teacher = Teacher.objects.create(
                first_name=body.get('first_name', ''),
                last_name=body.get('last_name', ''),
                email=body.get('email', ''),
                department=body.get('department', ''),
            )
            return JsonResponse({'id': teacher.id, 'message': 'Викладача створено'}, status=201)
        except IntegrityError as e:
            return JsonResponse({'error': str(e)}, status=400)

    return JsonResponse({'error': 'Method not allowed'}, status=405)


# ==================================================================
# Рівень 4: Масові операції з транзакціями
# ==================================================================

@csrf_exempt
def bulk_enroll(request):
    """POST — масове зарахування студентів на курс (транзакція)."""
    if request.method != 'POST':
        return JsonResponse({'error': 'Method not allowed'}, status=405)

    body = _parse_body(request)
    course_id = body.get('course_id')
    student_ids = body.get('student_ids', [])

    try:
        # Рівень 4: атомарна транзакція — або всі зараховуються, або ніхто
        with transaction.atomic():
            course = Course.objects.get(id=course_id)
            students = Student.objects.filter(id__in=student_ids)

            if students.count() != len(student_ids):
                raise ValueError("Деяких студентів не знайдено")

            for student in students:
                student.courses.add(course)

        cache.delete('students_list')
        cache.delete('courses_list')
        return JsonResponse({
            'message': f'{students.count()} студент(ів) зараховано на курс {course.code}'
        })
    except Course.DoesNotExist:
        return JsonResponse({'error': 'Курс не знайдено'}, status=404)
    except ValueError as e:
        return JsonResponse({'error': str(e)}, status=400)


@csrf_exempt
def bulk_grade(request):
    """POST — масове виставлення оцінок (транзакція)."""
    if request.method != 'POST':
        return JsonResponse({'error': 'Method not allowed'}, status=405)

    body = _parse_body(request)
    grades_data = body.get('grades', [])

    try:
        created = []
        # Рівень 4: одна транзакція для всіх оцінок
        with transaction.atomic():
            for g in grades_data:
                grade = Grade.objects.create(
                    student_id=g['student_id'],
                    course_id=g['course_id'],
                    grade_type=g.get('grade_type', 'homework'),
                    score=g['score'],
                    comment=g.get('comment', ''),
                )
                created.append(grade.id)

        cache.delete('courses_list')
        return JsonResponse({
            'message': f'Створено {len(created)} оцінок',
            'grade_ids': created
        }, status=201)
    except (KeyError, IntegrityError, Exception) as e:
        return JsonResponse({'error': f'Помилка: {e}. Жодну оцінку не збережено.'}, status=400)


# ==================================================================
# Рівень 3: Статистика (кешована)
# ==================================================================

def statistics(request):
    """GET — загальна статистика університету (кешована)."""
    cache_key = 'university_stats'
    data = cache.get(cache_key)

    if data is None:
        data = {
            'total_students': Student.objects.count(),
            'total_teachers': Teacher.objects.count(),
            'total_courses': Course.objects.count(),
            'total_lessons': Lesson.objects.count(),
            'total_grades': Grade.objects.count(),
            'average_score': round(
                Grade.objects.aggregate(avg=Avg('score'))['avg'] or 0, 1
            ),
            'courses_stats': list(
                Course.objects.annotate(
                    student_count=Count('students'),
                    avg_score=Avg('grades__score'),
                ).values('code', 'name', 'student_count', 'avg_score')
            ),
        }
        cache.set(cache_key, data, 300)

    return JsonResponse(data)


# ==================================================================
# Рівень 3: Очищення кешу
# ==================================================================

def clear_cache(request):
    """GET — очистити весь кеш."""
    cache.clear()
    return JsonResponse({'message': 'Кеш очищено'})
