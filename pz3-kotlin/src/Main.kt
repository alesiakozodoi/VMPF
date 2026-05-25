/**
 * Практичне заняття №3. Мобільна розробка на Android з Kotlin
 * Студент №6 | Завдання №6 з кожного рівня
 *
 * Запуск: kotlinc src/Main.kt -include-runtime -d pz3.jar && java -jar pz3.jar
 * Або:   kotlin src/Main.kt
 */

import java.io.File

fun main() {
    println("=" .repeat(55))
    println("  Практичне заняття №3 — Kotlin")
    println("  Студент №6 | Завдання №6 з кожного рівня")
    println("=".repeat(55))

    while (true) {
        println("\nОберіть завдання:")
        println("  1 — Рівень 1: Частка двох чисел (a / b)")
        println("  2 — Рівень 2: Гра «Кістки»")
        println("  3 — Рівень 3: Кількість речень у файлах")
        println("  4 — Рівень 4: Однакові рядки у файлах")
        println("  0 — Вихід")
        print("\nВаш вибір: ")

        when (readLine()?.trim()) {
            "1" -> level1Division()
            "2" -> level2DiceGame()
            "3" -> level3CountSentences()
            "4" -> level4FindDuplicateLines()
            "0" -> {
                println("До побачення!")
                return
            }
            else -> println("Невірний вибір, спробуйте ще раз.")
        }
    }
}


// ============================================================
// Рівень 1, Завдання 6
// Форма з двома полями для вводу чисел.
// Кнопка, при натисканні якої частка (a/b) виводиться на екран.
// ============================================================

fun level1Division() {
    println("\n${"=".repeat(50)}")
    println("Рівень 1: Обчислення частки двох чисел (a / b)")
    println("=".repeat(50))

    print("Введіть число a: ")
    val aInput = readLine()?.trim()
    val a = aInput?.toDoubleOrNull()

    if (a == null) {
        println("Помилка: '$aInput' — це не число.")
        return
    }

    print("Введіть число b: ")
    val bInput = readLine()?.trim()
    val b = bInput?.toDoubleOrNull()

    if (b == null) {
        println("Помилка: '$bInput' — це не число.")
        return
    }

    if (b == 0.0) {
        println("Помилка: ділення на нуль неможливе!")
        return
    }

    val result = a / b
    println("\nРезультат: $a / $b = $result")

    // Додатково: визначаємо тип результату
    if (result == result.toLong().toDouble()) {
        println("Результат є цілим числом: ${result.toLong()}")
    } else {
        println("Результат є дробовим числом")
    }
}


// ============================================================
// Рівень 2, Завдання 6
// Гра «Кістки»: два гравці кидають рівну кількість кубиків 1..6,
// перемагає той, у кого сума значень на кубиках більша.
// ============================================================

fun level2DiceGame() {
    println("\n${"=".repeat(50)}")
    println("Рівень 2: Гра «Кістки»")
    println("=".repeat(50))

    print("Скільки кубиків кидає кожен гравець? (1-10): ")
    val diceCount = readLine()?.trim()?.toIntOrNull()

    if (diceCount == null || diceCount < 1 || diceCount > 10) {
        println("Помилка: введіть число від 1 до 10.")
        return
    }

    println("\nГравець 1 кидає $diceCount кубик(ів)...")
    val player1Dice = List(diceCount) { (1..6).random() }
    val player1Sum = player1Dice.sum()
    println("  Кубики: ${player1Dice.joinToString(", ")}")
    println("  Сума: $player1Sum")

    println("\nГравець 2 кидає $diceCount кубик(ів)...")
    val player2Dice = List(diceCount) { (1..6).random() }
    val player2Sum = player2Dice.sum()
    println("  Кубики: ${player2Dice.joinToString(", ")}")
    println("  Сума: $player2Sum")

    println()
    when {
        player1Sum > player2Sum -> println("🏆 Перемагає Гравець 1! ($player1Sum > $player2Sum)")
        player2Sum > player1Sum -> println("🏆 Перемагає Гравець 2! ($player2Sum > $player1Sum)")
        else -> println("🤝 Нічия! ($player1Sum = $player2Sum)")
    }

    // Запитуємо чи хоче грати ще
    print("\nЗіграти ще раз? (так/ні): ")
    val again = readLine()?.trim()?.lowercase()
    if (again == "так" || again == "yes" || again == "y") {
        level2DiceGame()
    }
}


// ============================================================
// Рівень 3, Завдання 6
// Підрахуйте кількість речень в заданих користувачем файлах.
// ============================================================

fun level3CountSentences() {
    println("\n${"=".repeat(50)}")
    println("Рівень 3: Підрахунок речень у файлах")
    println("=".repeat(50))

    // Створюємо демо-файли для тестування
    createSampleFiles()

    println("Доступні демо-файли: sample1.txt, sample2.txt")
    print("Введіть шляхи до файлів через пробіл: ")
    val input = readLine()?.trim() ?: ""

    if (input.isEmpty()) {
        println("Не вказано жодного файлу.")
        return
    }

    val filePaths = input.split("\\s+".toRegex())
    var totalSentences = 0

    for (path in filePaths) {
        val file = File(path)
        if (!file.exists()) {
            println("\n⚠ Файл '$path' не знайдено.")
            continue
        }

        val text = file.readText()
        // Речення закінчуються на '.', '!', '?'
        // Використовуємо регулярний вираз для розбиття на речення
        val sentences = text.split(Regex("[.!?]+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val count = sentences.size
        totalSentences += count

        println("\n📄 Файл: $path")
        println("   Кількість речень: $count")
        println("   Речення:")
        sentences.forEachIndexed { index, sentence ->
            println("     ${index + 1}. ${sentence.take(80)}${if (sentence.length > 80) "..." else ""}")
        }
    }

    println("\n${"—".repeat(40)}")
    println("Загальна кількість речень у всіх файлах: $totalSentences")
}


// ============================================================
// Рівень 4, Завдання 6
// Знайдіть кількість однакових рядків в заданих файлах.
// ============================================================

fun level4FindDuplicateLines() {
    println("\n${"=".repeat(50)}")
    println("Рівень 4: Однакові рядки у файлах")
    println("=".repeat(50))

    // Створюємо демо-файли для тестування
    createSampleFiles()

    println("Доступні демо-файли: sample1.txt, sample2.txt")
    print("Введіть шляхи до файлів через пробіл: ")
    val input = readLine()?.trim() ?: ""

    if (input.isEmpty()) {
        println("Не вказано жодного файлу.")
        return
    }

    val filePaths = input.split("\\s+".toRegex())
    val fileLines = mutableMapOf<String, MutableList<String>>()

    // Зчитуємо рядки з усіх файлів
    for (path in filePaths) {
        val file = File(path)
        if (!file.exists()) {
            println("⚠ Файл '$path' не знайдено.")
            continue
        }
        val lines = file.readLines().map { it.trim() }.filter { it.isNotEmpty() }
        fileLines[path] = lines.toMutableList()
        println("📄 Файл '$path': ${lines.size} рядків")
    }

    if (fileLines.size < 2) {
        println("\nПотрібно мінімум 2 файли для порівняння.")
        return
    }

    // Знаходимо спільні рядки між файлами
    println("\n${"—".repeat(40)}")
    println("Пошук однакових рядків між файлами:\n")

    val allFiles = fileLines.keys.toList()
    var totalDuplicates = 0

    for (i in allFiles.indices) {
        for (j in i + 1 until allFiles.size) {
            val file1 = allFiles[i]
            val file2 = allFiles[j]
            val lines1 = fileLines[file1]!!
            val lines2 = fileLines[file2]!!

            // Знаходимо спільні рядки
            val commonLines = lines1.filter { it in lines2 }.distinct()

            println("📋 Порівняння: '$file1' та '$file2'")
            println("   Кількість однакових рядків: ${commonLines.size}")

            if (commonLines.isNotEmpty()) {
                println("   Однакові рядки:")
                commonLines.forEach { line ->
                    println("     • \"${line.take(70)}${if (line.length > 70) "..." else ""}\"")
                }
            }
            println()
            totalDuplicates += commonLines.size
        }
    }

    // Також знаходимо дублікати всередині кожного файлу
    println("Дублікати всередині кожного файлу:\n")
    for ((path, lines) in fileLines) {
        val duplicates = lines.groupBy { it }
            .filter { it.value.size > 1 }
            .map { "${it.key} (${it.value.size} разів)" }

        println("📄 Файл '$path':")
        if (duplicates.isEmpty()) {
            println("   Дублікатів немає")
        } else {
            duplicates.forEach { println("   • $it") }
        }
    }

    println("\n${"—".repeat(40)}")
    println("Загальна кількість спільних рядків між файлами: $totalDuplicates")
}


// ============================================================
// Допоміжна функція: створення демо-файлів
// ============================================================

fun createSampleFiles() {
    val sample1 = File("sample1.txt")
    if (!sample1.exists()) {
        sample1.writeText(
            """Kotlin — це сучасна мова програмування. Вона працює на JVM!
Android використовує Kotlin як основну мову. Це дуже зручно.
Програмування — це цікаво? Так, безумовно!
Kotlin підтримує функціональне програмування.
Вивчення нових мов розширює кругозір."""
        )
        println("✅ Створено демо-файл: sample1.txt")
    }

    val sample2 = File("sample2.txt")
    if (!sample2.exists()) {
        sample2.writeText(
            """Java — попередник Kotlin на платформі Android.
Kotlin підтримує функціональне програмування.
Android Studio — основне IDE для розробки. Чи не так?
Програмування — це цікаво? Без сумніву!
Вивчення нових мов розширює кругозір."""
        )
        println("✅ Створено демо-файл: sample2.txt")
    }
}
