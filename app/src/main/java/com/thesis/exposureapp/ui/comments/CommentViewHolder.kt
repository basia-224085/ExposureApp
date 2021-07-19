package com.thesis.exposureapp.ui.comments


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thesis.exposureapp.R
import com.thesis.exposureapp.models.Comment


class CommentViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.comment_adapter, parent, false)) {
    private var username: TextView = itemView.findViewById(R.id.username)
    private var date: TextView = itemView.findViewById(R.id.date)
    private var answer: TextView = itemView.findViewById(R.id.answer)
    private var ratingBar: RatingBar = itemView.findViewById(R.id.rating_bar)

    fun bind(comment: Comment) {
        username.text = comment.username
        date.text = comment.date
        answer.text = comment.answer
        if(comment.rating == -1.0f) {
            ratingBar.visibility = View.GONE
        }
        else {
            ratingBar.rating = comment.rating
        }
    }
}
