from django.contrib import admin
from .models import Transaction, Category


@admin.register(Category)
class CategoryAdmin(admin.ModelAdmin):
    list_display = ('name',)
    search_fields = ('name',)


@admin.register(Transaction)
class TransactionAdmin(admin.ModelAdmin):
    list_display = ('description', 'amount', 'transaction_type', 'category', 'created_at')
    list_filter = ('transaction_type', 'category', 'created_at')
    search_fields = ('description',)
