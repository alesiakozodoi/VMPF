from django.urls import path
from . import views

urlpatterns = [
    # Студенти
    path('students/', views.students_list, name='students_list'),
    path('students/<int:pk>/', views.student_detail, name='student_detail'),
    # Курси
    path('courses/', views.courses_list, name='courses_list'),
    path('courses/<int:pk>/', views.course_detail, name='course_detail'),
    # Оцінки
    path('grades/', views.grades_list, name='grades_list'),
    # Викладачі
    path('teachers/', views.teachers_list, name='teachers_list'),
    # Масові операції (Рівень 4: транзакції)
    path('bulk/enroll/', views.bulk_enroll, name='bulk_enroll'),
    path('bulk/grade/', views.bulk_grade, name='bulk_grade'),
    # Статистика (Рівень 3: кешування)
    path('statistics/', views.statistics, name='statistics'),
    path('clear-cache/', views.clear_cache, name='clear_cache'),
]
