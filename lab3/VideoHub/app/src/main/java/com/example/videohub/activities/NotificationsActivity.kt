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
import com.example.videohub.models.Notification
import java.text.SimpleDateFormat
import java.util.*

class NotificationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val rv = findViewById<RecyclerView>(R.id.rvNotifications)
        rv.layoutManager = LinearLayoutManager(this)

        val notifications = DataManager.getNotifications(this)
        rv.adapter = NotifAdapter(notifications) { notif ->
            DataManager.markAsRead(this, notif.id)
            rv.adapter = NotifAdapter(DataManager.getNotifications(this)) {}
        }

        val tvEmpty = findViewById<TextView>(R.id.tvNoNotifications)
        tvEmpty.visibility = if (notifications.isEmpty()) View.VISIBLE else View.GONE

        findViewById<Button>(R.id.btnNotifBack).setOnClickListener { finish() }
    }

    inner class NotifAdapter(
        private val items: List<Notification>,
        private val onMarkRead: (Notification) -> Unit
    ) : RecyclerView.Adapter<NotifAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvMsg: TextView = view.findViewById(R.id.tvNotifMessage)
            val tvDate: TextView = view.findViewById(R.id.tvNotifDate)
            val btnRead: Button = view.findViewById(R.id.btnMarkRead)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val n = items[position]
            holder.tvMsg.text = n.message
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            holder.tvDate.text = sdf.format(Date(n.createdAt))

            if (n.read) {
                holder.btnRead.visibility = View.GONE
                holder.itemView.alpha = 0.6f
            } else {
                holder.btnRead.visibility = View.VISIBLE
                holder.itemView.alpha = 1f
                holder.btnRead.setOnClickListener { onMarkRead(n) }
            }
        }

        override fun getItemCount() = items.size
    }
}
