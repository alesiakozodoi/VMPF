from django.contrib import admin
from .models import Student, Teacher, Course, Lesson, Grade

@admin.register(Student)
class StudentAdmin(admin.ModelAdmin):
    list_display = ('student_id', 'last_name', 'first_name', 'email')
    search_fields = ('last_name', 'student_id')
    filter_horizontal = ('courses',)

@admin.register(Teacher)
class TeacherAdmin(admin.ModelAdmin):
    list_display = ('last_name', 'first_name', 'department', 'email')
    search_fields = ('last_name', 'department')

@admin.register(Course)
class CourseAdmin(admin.ModelAdmin):
    list_display = ('code', 'name', 'credits')
    search_fields = ('code', 'name')
    filter_horizontal = ('teachers',)

@admin.register(Lesson)
class LessonAdmin(admin.ModelAdmin):
    list_display = ('course', 'teacher', 'lesson_type', 'topic', 'date', 'room')
    list_filter = ('lesson_type', 'course')

@admin.register(Grade)
class GradeAdmin(admin.ModelAdmin):
    list_display = ('student', 'course', 'grade_type', 'score', 'date')
    list_filter = ('grade_type', 'course')
