from django import forms
from .models import Transaction, Category


class TransactionForm(forms.ModelForm):
    """Форма для створення/редагування транзакції."""

    class Meta:
        model = Transaction
        fields = ['description', 'amount', 'transaction_type', 'category']
        widgets = {
            'description': forms.TextInput(attrs={
                'class': 'form-control',
                'placeholder': 'Опис транзакції',
            }),
            'amount': forms.NumberInput(attrs={
                'class': 'form-control',
                'placeholder': '0.00',
                'step': '0.01',
            }),
            'transaction_type': forms.Select(attrs={
                'class': 'form-control',
            }),
            'category': forms.Select(attrs={
                'class': 'form-control',
            }),
        }


class CategoryForm(forms.ModelForm):
    """Форма для створення категорії."""

    class Meta:
        model = Category
        fields = ['name']
        widgets = {
            'name': forms.TextInput(attrs={
                'class': 'form-control',
                'placeholder': 'Назва категорії',
            }),
        }


class TransactionFilterForm(forms.Form):
    """Форма для пошуку, сортування та фільтрації транзакцій (Рівень 3)."""

    search = forms.CharField(
        required=False,
        widget=forms.TextInput(attrs={
            'class': 'form-control',
            'placeholder': 'Пошук за описом...',
        }),
    )
    category = forms.ModelChoiceField(
        queryset=Category.objects.all(),
        required=False,
        empty_label='Усі категорії',
        widget=forms.Select(attrs={'class': 'form-control'}),
    )
    transaction_type = forms.ChoiceField(
        required=False,
        choices=[('', 'Усі типи')] + Transaction.TYPE_CHOICES,
        widget=forms.Select(attrs={'class': 'form-control'}),
    )
    sort_by = forms.ChoiceField(
        required=False,
        choices=[
            ('-created_at', 'Дата (нові спочатку)'),
            ('created_at', 'Дата (старі спочатку)'),
            ('-amount', 'Сума (більші спочатку)'),
            ('amount', 'Сума (менші спочатку)'),
        ],
        widget=forms.Select(attrs={'class': 'form-control'}),
    )
