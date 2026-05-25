package com.example.videohub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.videohub.R
import com.example.videohub.models.Video
import com.example.videohub.models.DataManager

class VideoAdapter(
    private var videos: List<Video>,
    private val onItemClick: (Video) -> Unit
) : RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvVideoTitle)
        val info: TextView = view.findViewById(R.id.tvVideoInfo)
        val likes: TextView = view.findViewById(R.id.tvLikes)
        val btnLike: ImageButton = view.findViewById(R.id.btnLike)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val video = videos[position]
        val context = holder.itemView.context
        val authorName = DataManager.getChannelName(context, video.authorId)

        holder.title.text = video.title
        holder.info.text = "$authorName • ${video.views} переглядів • ${video.comments.size} коментарів"
        holder.likes.text = "${video.likes.size}"

        val isLiked = DataManager.getCurrentUserId() in video.likes
        holder.btnLike.setImageResource(
            if (isLiked) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )

        holder.btnLike.setOnClickListener {
            DataManager.toggleLike(context, video.id)
            notifyItemChanged(position)
        }

        holder.itemView.setOnClickListener { onItemClick(video) }
    }

    override fun getItemCount() = videos.size

    fun updateData(newVideos: List<Video>) {
        videos = newVideos
        notifyDataSetChanged()
    }
}
