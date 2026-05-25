"""
Практичне заняття №1. Основи Python та робота з даними
Студент: №6 в журналі
Завдання: по одному з кожного рівня (завдання №2)
"""



def level_1():
    print("=" * 50)
    print("Рівень 1: Привітання користувача")
    print("=" * 50)

    name = input("Введіть ваше ім'я: ")
    print(f"Привіт, {name}! Радий тебе бачити!")



def level_2():
    print("\n" + "=" * 50)
    print("Рівень 2: Числа та їх квадрати (1–20)")
    print("=" * 50)

    numbers = list(range(1, 21))

    print(f"{'Число':<10}{'Квадрат':<10}")
    print("-" * 20)

    for num in numbers:
        print(f"{num:<10}{num ** 2:<10}")



def get_even_numbers(numbers):
    """Повертає новий список, що містить лише парні числа."""
    return [n for n in numbers if n % 2 == 0]


def level_3():
    print("\n" + "=" * 50)
    print("Рівень 3: Фільтрація парних чисел")
    print("=" * 50)

    sample_list = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 22, 33, 44]
    even_numbers = get_even_numbers(sample_list)

    print(f"Вхідний список:  {sample_list}")
    print(f"Парні числа:     {even_numbers}")

    # Додатково: введення від користувача
    user_input = input("\nВведіть свої числа через пробіл (або Enter для пропуску): ")
    if user_input.strip():
        try:
            user_numbers = [int(x) for x in user_input.split()]
            user_even = get_even_numbers(user_numbers)
            print(f"Ваш список:      {user_numbers}")
            print(f"Парні числа:     {user_even}")
        except ValueError:
            print("Помилка: введіть лише цілі числа через пробіл.")


def level_4():
    print("\n" + "=" * 50)
    print("Рівень 4: Робота з API (JSONPlaceholder)")
    print("=" * 50)

    try:
        import requests
    except ImportError:
        print("Бібліотека requests не встановлена.")
        print("Встановіть її командою: pip install requests")
        return

    url = "https://jsonplaceholder.typicode.com/users"

    try:
        response = requests.get(url, timeout=10)
        response.raise_for_status()

        users = response.json()

        print(f"\nОтримано {len(users)} користувачів з API:\n")
        header_name = "Ім'я"
        print(f"{'ID':<5}{header_name:<25}{'Email':<30}{'Місто':<15}")
        print("-" * 75)

        for user in users:
            print(
                f"{user['id']:<5}"
                f"{user['name']:<25}"
                f"{user['email']:<30}"
                f"{user['address']['city']:<15}"
            )

    except requests.exceptions.ConnectionError:
        print("Помилка: немає з'єднання з інтернетом.")
    except requests.exceptions.Timeout:
        print("Помилка: час очікування вичерпано.")
    except requests.exceptions.HTTPError as e:
        print(f"HTTP помилка: {e}")
    except Exception as e:
        print(f"Непередбачена помилка: {e}")



def main():
    print("Практичне заняття №1. Основи Python та робота з даними")
    print("Студент №6 | Завдання №2 з кожного рівня\n")

    level_1()
    level_2()
    level_3()
    level_4()

    print("\n" + "=" * 50)
    print("Усі завдання виконано!")
    print("=" * 50)


if __name__ == "__main__":
    main()
