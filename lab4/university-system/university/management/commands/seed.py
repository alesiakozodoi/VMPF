"""Команда для заповнення бази тестовими даними."""
from django.core.management.base import BaseCommand
from django.db import transaction
from django.utils import timezone
from university.models import Student, Teacher, Course, Lesson, Grade
import random


class Command(BaseCommand):
    help = 'Заповнити базу даних тестовими даними'

    def handle(self, *args, **options):
        with transaction.atomic():
            # Викладачі
            teachers = []
            teacher_data = [
                ('Іван', 'Петренко', 'petrenkoI@uni.ua', 'Кафедра інформатики'),
                ('Олена', 'Коваленко', 'kovalenkoO@uni.ua', 'Кафедра математики'),
                ('Андрій', 'Шевченко', 'shevchenkoA@uni.ua', 'Кафедра інформатики'),
                ('Марія', 'Бондаренко', 'bondarenkoM@uni.ua', 'Кафедра фізики'),
            ]
            for fn, ln, email, dept in teacher_data:
                t, _ = Teacher.objects.get_or_create(
                    email=email, defaults={
                        'first_name': fn, 'last_name': ln, 'department': dept
                    }
                )
                teachers.append(t)
            self.stdout.write(f'Створено {len(teachers)} викладачів')

            # Курси
            courses = []
            course_data = [
                ('CS101', 'Основи програмування', 4),
                ('CS201', 'Бази даних', 3),
                ('MATH101', 'Вища математика', 5),
                ('PHYS101', 'Загальна фізика', 4),
                ('CS301', 'Веб-розробка', 3),
            ]
            for code, name, credits in course_data:
                c, _ = Course.objects.get_or_create(
                    code=code, defaults={'name': name, 'credits': credits}
                )
                c.teachers.add(random.choice(teachers))
                courses.append(c)
            self.stdout.write(f'Створено {len(courses)} курсів')

            # Студенти
            students = []
            student_data = [
                ('Олексій', 'Мельник', 'melnyk@student.ua', 'STD001'),
                ('Анна', 'Ткаченко', 'tkachenko@student.ua', 'STD002'),
                ('Дмитро', 'Кравченко', 'kravchenko@student.ua', 'STD003'),
                ('Юлія', 'Мороз', 'moroz@student.ua', 'STD004'),
                ('Максим', 'Лисенко', 'lysenko@student.ua', 'STD005'),
                ('Катерина', 'Шевчук', 'shevchuk@student.ua', 'STD006'),
            ]
            for fn, ln, email, sid in student_data:
                s, _ = Student.objects.get_or_create(
                    student_id=sid, defaults={
                        'first_name': fn, 'last_name': ln, 'email': email
                    }
                )
                s.courses.set(random.sample(courses, k=random.randint(2, 4)))
                students.append(s)
            self.stdout.write(f'Створено {len(students)} студентів')

            # Заняття
            lesson_count = 0
            for course in courses:
                for i in range(3):
                    Lesson.objects.get_or_create(
                        course=course,
                        topic=f'Тема {i+1} з {course.name}',
                        defaults={
                            'teacher': course.teachers.first() or teachers[0],
                            'lesson_type': random.choice(['lecture', 'practice', 'lab']),
                            'date': timezone.now(),
                            'room': f'Ауд. {random.randint(100, 500)}',
                        }
                    )
                    lesson_count += 1
            self.stdout.write(f'Створено {lesson_count} занять')

            # Оцінки
            grade_count = 0
            for student in students:
                for course in student.courses.all():
                    Grade.objects.get_or_create(
                        student=student,
                        course=course,
                        grade_type='exam',
                        defaults={
                            'score': random.randint(50, 100),
                            'comment': 'Автоматично згенеровано',
                        }
                    )
                    grade_count += 1
            self.stdout.write(f'Створено {grade_count} оцінок')

        self.stdout.write(self.style.SUCCESS('База даних заповнена!'))
