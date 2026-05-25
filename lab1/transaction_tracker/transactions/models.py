from django.db import models


class Category(models.Model):
    """Модель категорії для категоризації транзакцій (Рівень 2)."""
    name = models.CharField("Назва категорії", max_length=100, unique=True)

    class Meta:
        verbose_name = "Категорія"
        verbose_name_plural = "Категорії"
        ordering = ['name']

    def __str__(self):
        return self.name


class Transaction(models.Model):
    """Модель транзакції (Рівень 1 + Рівень 2)."""

    DEBIT = 'debit'
    CREDIT = 'credit'
    TYPE_CHOICES = [
        (DEBIT, 'Дебет (дохід)'),
        (CREDIT, 'Кредит (витрата)'),
    ]

    description = models.CharField("Опис", max_length=255)
    amount = models.DecimalField("Сума", max_digits=12, decimal_places=2)
    transaction_type = models.CharField(
        "Тип транзакції",
        max_length=6,
        choices=TYPE_CHOICES,
        default=DEBIT,
    )
    category = models.ForeignKey(
        Category,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        verbose_name="Категорія",
        related_name="transactions",
    )
    created_at = models.DateTimeField("Дата створення", auto_now_add=True)

    class Meta:
        verbose_name = "Транзакція"
        verbose_name_plural = "Транзакції"
        ordering = ['-created_at']

    def __str__(self):
        type_label = "+" if self.transaction_type == self.DEBIT else "-"
        return f"{self.description} ({type_label}{self.amount})"
