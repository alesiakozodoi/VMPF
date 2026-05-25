from django.urls import path
from . import views

urlpatterns = [
    path('', views.transaction_list, name='transaction_list'),
    path('add/', views.transaction_create, name='transaction_create'),
    path('delete/<int:pk>/', views.transaction_delete, name='transaction_delete'),
    path('categories/', views.category_list, name='category_list'),
    path('categories/add/', views.category_create, name='category_create'),
    path('categories/<int:pk>/', views.category_transactions, name='category_transactions'),
    path('budget/', views.budget, name='budget'),
]
