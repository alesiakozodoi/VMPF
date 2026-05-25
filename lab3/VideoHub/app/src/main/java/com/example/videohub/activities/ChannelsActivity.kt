package com.example.videohub.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videohub.R
import com.example.videohub.models.DataManager
import com.example.videohub.models.User

class ChannelsActivity : AppCompatActivity() {

    private lateinit var adapter: ChannelAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channels)

        val rv = findViewById<RecyclerView>(R.id.rvChannels)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = ChannelAdapter(DataManager.getAllUsers(this)) {
            DataManager.toggleSubscription(this, it.id)
            adapter.updateData(DataManager.getAllUsers(this))
        }
        rv.adapter = adapter

        findViewById<Button>(R.id.btnChannelsBack).setOnClickListener { finish() }
    }

    // Вбудований адаптер для каналів
    inner class ChannelAdapter(
        private var users: List<User>,
        private val onSubscribe: (User) -> Unit
    ) : RecyclerView.Adapter<ChannelAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvChannelName)
            val tvInfo: TextView = view.findViewById(R.id.tvChannelInfo)
            val btnSub: Button = view.findViewById(R.id.btnChannelSubscribe)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val user = users[position]
            val videoCount = DataManager.getVideos(holder.itemView.context).count { it.authorId == user.id }
            holder.tvName.text = user.channel
            holder.tvInfo.text = "@${user.username} • ${user.subscribers.size} підписників • $videoCount відео"

            if (user.id == DataManager.getCurrentUserId()) {
                holder.btnSub.visibility = View.GONE
            } else {
                holder.btnSub.visibility = View.VISIBLE
                val subscribed = DataManager.isSubscribed(holder.itemView.context, user.id)
                holder.btnSub.text = if (subscribed) "✓ Підписано" else "Підписатися"
                holder.btnSub.setOnClickListener { onSubscribe(user) }
            }
        }

        override fun getItemCount() = users.size

        fun updateData(newUsers: List<User>) {
            users = newUsers
            notifyDataSetChanged()
        }
    }
}
