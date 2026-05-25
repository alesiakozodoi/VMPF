from django.shortcuts import render, redirect, get_object_or_404
from django.db.models import Sum, Q
from .models import Transaction, Category
from .forms import TransactionForm, CategoryForm, TransactionFilterForm


# ------------------------------------------------------------------
# Рівень 1: Список транзакцій та додавання нових
# Рівень 3: Пошук, сортування, фільтрація
# ------------------------------------------------------------------

def transaction_list(request):
    """Головна сторінка зі списком транзакцій, фільтрацією та бюджетом."""
    transactions = Transaction.objects.all()
    filter_form = TransactionFilterForm(request.GET)

    # Рівень 3: фільтрація, пошук, сортування
    if filter_form.is_valid():
        search = filter_form.cleaned_data.get('search')
        category = filter_form.cleaned_data.get('category')
        transaction_type = filter_form.cleaned_data.get('transaction_type')
        sort_by = filter_form.cleaned_data.get('sort_by')

        if search:
            transactions = transactions.filter(description__icontains=search)
        if category:
            transactions = transactions.filter(category=category)
        if transaction_type:
            transactions = transactions.filter(transaction_type=transaction_type)
        if sort_by:
            transactions = transactions.order_by(sort_by)

    # Рівень 4: бюджетування — підрахунок доходів, витрат, балансу
    all_transactions = Transaction.objects.all()
    total_income = all_transactions.filter(
        transaction_type=Transaction.DEBIT
    ).aggregate(total=Sum('amount'))['total'] or 0

    total_expense = all_transactions.filter(
        transaction_type=Transaction.CREDIT
    ).aggregate(total=Sum('amount'))['total'] or 0

    balance = total_income - total_expense

    context = {
        'transactions': transactions,
        'filter_form': filter_form,
        'total_income': total_income,
        'total_expense': total_expense,
        'balance': balance,
    }
    return render(request, 'transactions/transaction_list.html', context)


def transaction_create(request):
    """Створення нової транзакції."""
    if request.method == 'POST':
        form = TransactionForm(request.POST)
        if form.is_valid():
            form.save()
            return redirect('transaction_list')
    else:
        form = TransactionForm()

    return render(request, 'transactions/transaction_form.html', {
        'form': form,
        'title': 'Додати транзакцію',
    })


def transaction_delete(request, pk):
    """Видалення транзакції."""
    transaction = get_object_or_404(Transaction, pk=pk)
    if request.method == 'POST':
        transaction.delete()
        return redirect('transaction_list')
    return render(request, 'transactions/transaction_confirm_delete.html', {
        'transaction': transaction,
    })


# ------------------------------------------------------------------
# Рівень 2: Категорії та перегляд транзакцій за категоріями
# ------------------------------------------------------------------

def category_list(request):
    """Список категорій з кількістю транзакцій та сумами."""
    categories = Category.objects.all()
    category_data = []

    for cat in categories:
        cat_transactions = cat.transactions.all()
        income = cat_transactions.filter(
            transaction_type=Transaction.DEBIT
        ).aggregate(total=Sum('amount'))['total'] or 0
        expense = cat_transactions.filter(
            transaction_type=Transaction.CREDIT
        ).aggregate(total=Sum('amount'))['total'] or 0

        category_data.append({
            'category': cat,
            'count': cat_transactions.count(),
            'income': income,
            'expense': expense,
            'balance': income - expense,
        })

    return render(request, 'transactions/category_list.html', {
        'category_data': category_data,
    })


def category_create(request):
    """Створення нової категорії."""
    if request.method == 'POST':
        form = CategoryForm(request.POST)
        if form.is_valid():
            form.save()
            return redirect('category_list')
    else:
        form = CategoryForm()

    return render(request, 'transactions/transaction_form.html', {
        'form': form,
        'title': 'Додати категорію',
    })


def category_transactions(request, pk):
    """Перегляд транзакцій конкретної категорії."""
    category = get_object_or_404(Category, pk=pk)
    transactions = category.transactions.all()

    return render(request, 'transactions/category_transactions.html', {
        'category': category,
        'transactions': transactions,
    })


# ------------------------------------------------------------------
# Рівень 4: Сторінка бюджету
# ------------------------------------------------------------------

def budget(request):
    """Сторінка бюджетування: доходи, витрати, баланс за категоріями."""
    categories = Category.objects.all()
    budget_data = []

    for cat in categories:
        cat_transactions = cat.transactions.all()
        income = cat_transactions.filter(
            transaction_type=Transaction.DEBIT
        ).aggregate(total=Sum('amount'))['total'] or 0
        expense = cat_transactions.filter(
            transaction_type=Transaction.CREDIT
        ).aggregate(total=Sum('amount'))['total'] or 0

        budget_data.append({
            'category': cat,
            'income': income,
            'expense': expense,
            'balance': income - expense,
        })

    total_income = Transaction.objects.filter(
        transaction_type=Transaction.DEBIT
    ).aggregate(total=Sum('amount'))['total'] or 0

    total_expense = Transaction.objects.filter(
        transaction_type=Transaction.CREDIT
    ).aggregate(total=Sum('amount'))['total'] or 0

    return render(request, 'transactions/budget.html', {
        'budget_data': budget_data,
        'total_income': total_income,
        'total_expense': total_expense,
        'balance': total_income - total_expense,
    })
