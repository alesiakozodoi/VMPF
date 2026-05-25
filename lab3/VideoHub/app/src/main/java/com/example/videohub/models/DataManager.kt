package com.example.videohub.models

import android.content.Context
import com.google.gson.Gson

/**
 * Менеджер даних — зберігає та завантажує дані з SharedPreferences у форматі JSON.
 * Рівень 4: оптимізація через кешування даних у пам'яті.
 */
object DataManager {

    private const val PREFS_NAME = "videohub_data"
    private const val DATA_KEY = "app_data"
    private val gson = Gson()

    // Кешування в пам'яті (Рівень 4)
    private var cachedData: AppData? = null
    private var currentUserId: Int = -1

    fun init(context: Context) {
        if (cachedData == null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(DATA_KEY, null)
            cachedData = if (json != null) {
                gson.fromJson(json, AppData::class.java)
            } else {
                createInitialData()
            }
            save(context)
        }
    }

    private fun save(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(DATA_KEY, gson.toJson(cachedData)).apply()
    }

    fun getData(context: Context): AppData {
        if (cachedData == null) init(context)
        return cachedData!!
    }

    fun saveData(context: Context) { save(context) }

    // --- Авторизація (Рівень 3) ---

    fun setCurrentUser(userId: Int) { currentUserId = userId }
    fun getCurrentUserId(): Int = currentUserId

    fun getCurrentUser(context: Context): User? {
        return getData(context).users.find { it.id == currentUserId }
    }

    fun login(context: Context, username: String, password: String): User? {
        val user = getData(context).users.find {
            it.username == username && it.password == password
        }
        if (user != null) currentUserId = user.id
        return user
    }

    fun register(context: Context, username: String, password: String, channel: String): User? {
        val data = getData(context)
        if (data.users.any { it.username == username }) return null
        val user = User(
            id = data.nextUserId++,
            username = username,
            password = password,
            channel = channel.ifEmpty { "$username Channel" }
        )
        data.users.add(user)
        currentUserId = user.id
        save(context)
        return user
    }

    // --- Відео (Рівень 1 + 2) ---

    fun getVideos(context: Context): List<Video> = getData(context).videos.toList()

    fun getVideo(context: Context, id: Int): Video? =
        getData(context).videos.find { it.id == id }

    fun addVideo(context: Context, title: String, description: String, url: String): Video {
        val data = getData(context)
        val video = Video(
            id = data.nextVideoId++,
            title = title,
            description = description,
            url = url,
            authorId = currentUserId
        )
        data.videos.add(0, video)

        // Рівень 4: сповіщення підписникам
        val author = data.users.find { it.id == currentUserId }
        author?.subscribers?.forEach { subId ->
            data.notifications.add(
                Notification(
                    id = data.nextNotificationId++,
                    userId = subId,
                    message = "Нове відео \"$title\" на каналі \"${author.channel}\"",
                    videoId = video.id
                )
            )
        }

        save(context)
        return video
    }

    fun deleteVideo(context: Context, videoId: Int): Boolean {
        val data = getData(context)
        val video = data.videos.find { it.id == videoId } ?: return false
        if (video.authorId != currentUserId && getCurrentUser(context)?.role != "admin") return false
        data.videos.remove(video)
        save(context)
        return true
    }

    // Рівень 1: Лайки
    fun toggleLike(context: Context, videoId: Int): Boolean {
        val data = getData(context)
        val video = data.videos.find { it.id == videoId } ?: return false
        if (currentUserId in video.likes) {
            video.likes.remove(currentUserId)
        } else {
            video.likes.add(currentUserId)
        }
        save(context)
        return currentUserId in video.likes
    }

    // Рівень 2: Коментарі
    fun addComment(context: Context, videoId: Int, text: String): Comment? {
        val data = getData(context)
        val video = data.videos.find { it.id == videoId } ?: return null
        val comment = Comment(id = data.nextCommentId++, userId = currentUserId, text = text)
        video.comments.add(comment)
        save(context)
        return comment
    }

    // Рівень 3: Поділитися
    fun shareVideo(context: Context, videoId: Int, targetUserId: Int): Boolean {
        val data = getData(context)
        val video = data.videos.find { it.id == videoId } ?: return false
        val target = data.users.find { it.id == targetUserId } ?: return false
        if (targetUserId !in video.sharedWith) {
            video.sharedWith.add(targetUserId)
        }
        val sender = data.users.find { it.id == currentUserId }
        data.notifications.add(
            Notification(
                id = data.nextNotificationId++,
                userId = targetUserId,
                message = "${sender?.username} поділився відео \"${video.title}\"",
                videoId = videoId
            )
        )
        save(context)
        return true
    }

    fun getSharedVideos(context: Context): List<Video> =
        getData(context).videos.filter { currentUserId in it.sharedWith }

    // Рівень 4: Підписки
    fun toggleSubscription(context: Context, channelOwnerId: Int): Boolean {
        val data = getData(context)
        val owner = data.users.find { it.id == channelOwnerId } ?: return false
        return if (currentUserId in owner.subscribers) {
            owner.subscribers.remove(currentUserId)
            save(context)
            false
        } else {
            owner.subscribers.add(currentUserId)
            save(context)
            true
        }
    }

    fun isSubscribed(context: Context, channelOwnerId: Int): Boolean {
        val owner = getData(context).users.find { it.id == channelOwnerId }
        return owner?.subscribers?.contains(currentUserId) == true
    }

    // Рівень 4: Сповіщення
    fun getNotifications(context: Context): List<Notification> =
        getData(context).notifications
            .filter { it.userId == currentUserId }
            .sortedByDescending { it.createdAt }

    fun getUnreadCount(context: Context): Int =
        getNotifications(context).count { !it.read }

    fun markAsRead(context: Context, notifId: Int) {
        val data = getData(context)
        data.notifications.find { it.id == notifId && it.userId == currentUserId }?.read = true
        save(context)
    }

    fun getUsername(context: Context, userId: Int): String =
        getData(context).users.find { it.id == userId }?.username ?: "Невідомий"

    fun getChannelName(context: Context, userId: Int): String =
        getData(context).users.find { it.id == userId }?.channel ?: ""

    fun getAllUsers(context: Context): List<User> = getData(context).users.toList()

    // --- Початкові дані ---
    private fun createInitialData(): AppData {
        val data = AppData()
        data.users.add(User(data.nextUserId++, "admin", "admin123", "admin", "Admin Channel"))
        data.users.add(User(data.nextUserId++, "user1", "user123", "user", "Cool Videos"))

        data.videos.add(Video(data.nextVideoId++, "Вступ до Kotlin", "Базовий курс з Kotlin", "", 1, 150))
        data.videos.add(Video(data.nextVideoId++, "Android розробка", "Основи Android", "", 2, 230))
        data.videos.add(Video(data.nextVideoId++, "Jetpack Compose", "Новий UI фреймворк", "", 1, 95))
        return data
    }
}
