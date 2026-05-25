package com.example.videohub.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videohub.R
import com.example.videohub.adapters.VideoAdapter
import com.example.videohub.models.DataManager

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: VideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "VideoHub"

        val recyclerView = findViewById<RecyclerView>(R.id.rvVideos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = VideoAdapter(DataManager.getVideos(this)) { video ->
            val intent = Intent(this, VideoDetailActivity::class.java)
            intent.putExtra("video_id", video.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        adapter.updateData(DataManager.getVideos(this))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // Рівень 4: показати кількість сповіщень
        val notifCount = DataManager.getUnreadCount(this)
        val notifItem = menu.findItem(R.id.action_notifications)
        notifItem.title = if (notifCount > 0) "Сповіщення ($notifCount)" else "Сповіщення"

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_upload -> {
                startActivity(Intent(this, UploadActivity::class.java))
                true
            }
            R.id.action_channels -> {
                startActivity(Intent(this, ChannelsActivity::class.java))
                true
            }
            R.id.action_notifications -> {
                startActivity(Intent(this, NotificationsActivity::class.java))
                true
            }
            R.id.action_logout -> {
                DataManager.setCurrentUser(-1)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
