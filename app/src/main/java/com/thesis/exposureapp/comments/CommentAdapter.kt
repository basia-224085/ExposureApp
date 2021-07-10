package com.thesis.exposureapp.comments


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thesis.exposureapp.models.Comment

class CommentAdapter constructor(private val list: List<Comment>) :
    RecyclerView.Adapter<CommentViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CommentViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment: Comment = list[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int = list.size
}