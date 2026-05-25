"""
Рівень 1: Створення складної бази даних для системи керування університетом.
5 таблиць: Студенти, Викладачі, Курси, Заняття, Оцінки.
Зв'язки: один-до-багатьох, багато-до-багатьох.
"""

from django.db import models


class Teacher(models.Model):
    """Викладач"""
    first_name = models.CharField("Ім'я", max_length=100)
    last_name = models.CharField("Прізвище", max_length=100)
    email = models.EmailField("Email", unique=True)
    department = models.CharField("Кафедра", max_length=200)
    hire_date = models.DateField("Дата прийому на роботу", auto_now_add=True)

    class Meta:
        verbose_name = "Викладач"
        verbose_name_plural = "Викладачі"
        # Рівень 3: індекс для пошуку
        indexes = [
            models.Index(fields=['last_name'], name='idx_teacher_lastname'),
            models.Index(fields=['department'], name='idx_teacher_dept'),
        ]

    def __str__(self):
        return f"{self.last_name} {self.first_name}"


class Course(models.Model):
    """Курс — зв'язок багато-до-багатьох з Викладачами"""
    name = models.CharField("Назва курсу", max_length=200)
    code = models.CharField("Код курсу", max_length=20, unique=True)
    credits = models.PositiveIntegerField("Кредити", default=3)
    description = models.TextField("Опис", blank=True)
    # Багато-до-багатьох: один курс може мати кількох викладачів
    teachers = models.ManyToManyField(
        Teacher, verbose_name="Викладачі", related_name="courses", blank=True
    )

    class Meta:
        verbose_name = "Курс"
        verbose_name_plural = "Курси"
        indexes = [
            models.Index(fields=['code'], name='idx_course_code'),
        ]

    def __str__(self):
        return f"{self.code} — {self.name}"


class Student(models.Model):
    """Студент — зв'язок багато-до-багатьох з Курсами"""
    first_name = models.CharField("Ім'я", max_length=100)
    last_name = models.CharField("Прізвище", max_length=100)
    email = models.EmailField("Email", unique=True)
    student_id = models.CharField("Номер студентського", max_length=20, unique=True)
    enrollment_date = models.DateField("Дата зарахування", auto_now_add=True)
    # Багато-до-багатьох: студент записується на кілька курсів
    courses = models.ManyToManyField(
        Course, verbose_name="Курси", related_name="students", blank=True
    )

    class Meta:
        verbose_name = "Студент"
        verbose_name_plural = "Студенти"
        indexes = [
            models.Index(fields=['last_name'], name='idx_student_lastname'),
            models.Index(fields=['student_id'], name='idx_student_id'),
        ]

    def __str__(self):
        return f"{self.student_id} — {self.last_name} {self.first_name}"


class Lesson(models.Model):
    """Заняття — зв'язок один-до-багатьох з Курсом та Викладачем"""
    LESSON_TYPES = [
        ('lecture', 'Лекція'),
        ('practice', 'Практика'),
        ('lab', 'Лабораторна'),
        ('seminar', 'Семінар'),
    ]

    # Один-до-багатьох: курс має багато занять
    course = models.ForeignKey(
        Course, on_delete=models.CASCADE, verbose_name="Курс", related_name="lessons"
    )
    # Один-до-багатьох: викладач веде багато занять
    teacher = models.ForeignKey(
        Teacher, on_delete=models.CASCADE, verbose_name="Викладач", related_name="lessons"
    )
    lesson_type = models.CharField("Тип заняття", max_length=10, choices=LESSON_TYPES)
    topic = models.CharField("Тема", max_length=300)
    date = models.DateTimeField("Дата та час")
    room = models.CharField("Аудиторія", max_length=50)

    class Meta:
        verbose_name = "Заняття"
        verbose_name_plural = "Заняття"
        ordering = ['-date']
        indexes = [
            models.Index(fields=['date'], name='idx_lesson_date'),
            models.Index(fields=['course', 'date'], name='idx_lesson_course_date'),
        ]

    def __str__(self):
        return f"{self.course.code} — {self.topic} ({self.get_lesson_type_display()})"


class Grade(models.Model):
    """Оцінка — зв'язок один-до-багатьох зі Студентом та Курсом"""
    GRADE_TYPES = [
        ('exam', 'Іспит'),
        ('test', 'Залік'),
        ('homework', 'Домашнє завдання'),
        ('lab', 'Лабораторна робота'),
        ('project', 'Курсовий проект'),
    ]

    # Один-до-багатьох: студент має багато оцінок
    student = models.ForeignKey(
        Student, on_delete=models.CASCADE, verbose_name="Студент", related_name="grades"
    )
    # Один-до-багатьох: курс має багато оцінок
    course = models.ForeignKey(
        Course, on_delete=models.CASCADE, verbose_name="Курс", related_name="grades"
    )
    grade_type = models.CharField("Тип оцінки", max_length=10, choices=GRADE_TYPES)
    score = models.PositiveIntegerField("Бал (0-100)")
    date = models.DateField("Дата", auto_now_add=True)
    comment = models.TextField("Коментар", blank=True)

    class Meta:
        verbose_name = "Оцінка"
        verbose_name_plural = "Оцінки"
        ordering = ['-date']
        # Обмеження: один студент — одна оцінка за тип на курсі
        constraints = [
            models.CheckConstraint(
                condition=models.Q(score__lte=100),
                name='grade_score_max_100'
            ),
        ]
        indexes = [
            models.Index(fields=['student', 'course'], name='idx_grade_student_course'),
        ]

    def __str__(self):
        return f"{self.student.last_name} — {self.course.code}: {self.score}"
