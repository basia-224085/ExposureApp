package com.thesis.exposureapp.ui.forum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.thesis.exposureapp.R
import com.thesis.exposureapp.models.ForumThread

class ThreadViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.pic_and_text_adapter, parent, false)) {
    private var questionView: TextView = itemView.findViewById(R.id.text)
    private val picView: ImageView = itemView.findViewById(R.id.picture)
    fun bind(ft: ForumThread, listener: ContentListener) {
        questionView.text = ft.question
        questionView.setPadding(30)
        picView.visibility = View.GONE
        itemView.setOnClickListener {
            listener.onItemClicked(ft)
        }
    }

    interface ContentListener {
        fun onItemClicked(clickedThread: ForumThread) // implemented in ForumFragment
    }
}