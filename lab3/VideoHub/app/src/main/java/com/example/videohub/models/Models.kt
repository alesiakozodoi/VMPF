package com.example.videohub.models

/** Користувач (Рівень 3: авторизація та управління правами) */
data class User(
    val id: Int,
    val username: String,
    val password: String,
    val role: String = "user",         // "admin" або "user"
    val channel: String = "",
    val subscribers: MutableList<Int> = mutableListOf()
)

/** Відео (Рівень 1 + 2) */
data class Video(
    val id: Int,
    var title: String,
    var description: String,
    val url: String = "",
    val authorId: Int,
    var views: Int = 0,
    var likes: MutableList<Int> = mutableListOf(),     // Рівень 1: лайки
    val comments: MutableList<Comment> = mutableListOf(), // Рівень 2: коментарі
    val sharedWith: MutableList<Int> = mutableListOf(),  // Рівень 3: поділитися
    val createdAt: Long = System.currentTimeMillis()
)

/** Коментар (Рівень 2) */
data class Comment(
    val id: Int,
    val userId: Int,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

/** Сповіщення (Рівень 4: підписки та сповіщення) */
data class Notification(
    val id: Int,
    val userId: Int,
    val message: String,
    val videoId: Int = 0,
    var read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/** Обгортка для зберігання всіх даних у JSON */
data class AppData(
    val users: MutableList<User> = mutableListOf(),
    val videos: MutableList<Video> = mutableListOf(),
    val notifications: MutableList<Notification> = mutableListOf(),
    var nextUserId: Int = 1,
    var nextVideoId: Int = 1,
    var nextCommentId: Int = 1,
    var nextNotificationId: Int = 1
)
