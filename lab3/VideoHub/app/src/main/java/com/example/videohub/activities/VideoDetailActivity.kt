package com.example.videohub.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videohub.R
import com.example.videohub.adapters.CommentAdapter
import com.example.videohub.models.DataManager

class VideoDetailActivity : AppCompatActivity() {

    private var videoId: Int = 0
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_detail)

        videoId = intent.getIntExtra("video_id", 0)
        loadVideo()
    }

    private fun loadVideo() {
        val video = DataManager.getVideo(this, videoId) ?: run {
            Toast.makeText(this, "Відео не знайдено", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Збільшуємо перегляди
        video.views++
        DataManager.saveData(this)

        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvDescription = findViewById<TextView>(R.id.tvDetailDescription)
        val tvStats = findViewById<TextView>(R.id.tvDetailStats)
        val tvChannel = findViewById<TextView>(R.id.tvDetailChannel)
        val btnLike = findViewById<Button>(R.id.btnDetailLike)
        val btnShare = findViewById<Button>(R.id.btnDetailShare)
        val btnSubscribe = findViewById<Button>(R.id.btnDetailSubscribe)
        val btnDelete = findViewById<Button>(R.id.btnDetailDelete)
        val etComment = findViewById<EditText>(R.id.etComment)
        val btnComment = findViewById<Button>(R.id.btnSendComment)
        val rvComments = findViewById<RecyclerView>(R.id.rvComments)
        val tvCommentsTitle = findViewById<TextView>(R.id.tvCommentsTitle)

        tvTitle.text = video.title
        tvDescription.text = video.description
        tvStats.text = "${video.views} переглядів"

        val channelName = DataManager.getChannelName(this, video.authorId)
        val subsCount = DataManager.getData(this).users.find { it.id == video.authorId }?.subscribers?.size ?: 0
        tvChannel.text = "$channelName ($subsCount підписників)"

        // Рівень 1: Лайки
        updateLikeButton(btnLike, video.likes.size, DataManager.getCurrentUserId() in video.likes)
        btnLike.setOnClickListener {
            val isLiked = DataManager.toggleLike(this, videoId)
            val v = DataManager.getVideo(this, videoId)!!
            updateLikeButton(btnLike, v.likes.size, isLiked)
        }

        // Рівень 3: Поділитися
        btnShare.setOnClickListener { showShareDialog() }

        // Рівень 4: Підписка
        val isOwner = video.authorId == DataManager.getCurrentUserId()
        if (isOwner) {
            btnSubscribe.visibility = android.view.View.GONE
        } else {
            updateSubscribeButton(btnSubscribe, video.authorId)
            btnSubscribe.setOnClickListener {
                DataManager.toggleSubscription(this, video.authorId)
                updateSubscribeButton(btnSubscribe, video.authorId)
                loadVideo()
            }
        }

        // Рівень 3: Видалення (автор або адмін)
        val currentUser = DataManager.getCurrentUser(this)
        if (isOwner || currentUser?.role == "admin") {
            btnDelete.visibility = android.view.View.VISIBLE
            btnDelete.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Видалити відео?")
                    .setPositiveButton("Так") { _, _ ->
                        DataManager.deleteVideo(this, videoId)
                        finish()
                    }
                    .setNegativeButton("Ні", null)
                    .show()
            }
        } else {
            btnDelete.visibility = android.view.View.GONE
        }

        // Рівень 2: Коментарі
        tvCommentsTitle.text = "Коментарі (${video.comments.size})"
        commentAdapter = CommentAdapter(video.comments)
        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter = commentAdapter

        btnComment.setOnClickListener {
            val text = etComment.text.toString().trim()
            if (text.isNotEmpty()) {
                DataManager.addComment(this, videoId, text)
                etComment.text.clear()
                val updated = DataManager.getVideo(this, videoId)!!
                commentAdapter.updateData(updated.comments)
                tvCommentsTitle.text = "Коментарі (${updated.comments.size})"
            }
        }
    }

    private fun updateLikeButton(btn: Button, count: Int, isLiked: Boolean) {
        btn.text = if (isLiked) "❤ $count" else "🤍 $count"
    }

    private fun updateSubscribeButton(btn: Button, authorId: Int) {
        val subscribed = DataManager.isSubscribed(this, authorId)
        btn.text = if (subscribed) "✓ Підписано" else "+ Підписатися"
    }

    // Рівень 3: Діалог "Поділитися відео"
    private fun showShareDialog() {
        val users = DataManager.getAllUsers(this)
            .filter { it.id != DataManager.getCurrentUserId() }
        val names = users.map { it.username }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Поділитися відео")
            .setItems(names) { _, which ->
                val targetUser = users[which]
                DataManager.shareVideo(this, videoId, targetUser.id)
                Toast.makeText(this, "Надіслано ${targetUser.username}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }
}
