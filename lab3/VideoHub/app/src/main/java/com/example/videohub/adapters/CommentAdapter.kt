package com.example.videohub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.videohub.R
import com.example.videohub.models.Comment
import com.example.videohub.models.DataManager
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter(
    private var comments: List<Comment>
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val author: TextView = view.findViewById(R.id.tvCommentAuthor)
        val text: TextView = view.findViewById(R.id.tvCommentText)
        val date: TextView = view.findViewById(R.id.tvCommentDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]
        val context = holder.itemView.context
        holder.author.text = DataManager.getUsername(context, comment.userId)
        holder.text.text = comment.text
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        holder.date.text = sdf.format(Date(comment.createdAt))
    }

    override fun getItemCount() = comments.size

    fun updateData(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
    }
}
