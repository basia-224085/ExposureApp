package com.thesis.exposureapp.ui.mix_and_match.matches


import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.thesis.exposureapp.R

class MatchViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.pic_and_text_adapter, parent, false)) {
    private var nameAndSurnameText: TextView = itemView.findViewById(R.id.text)
    private var profilePicture: ImageView = itemView.findViewById(R.id.picture)

    fun bind(match: Triple<String, String, Uri>, listener: ContentListener) {
        nameAndSurnameText.text = match.second
        Picasso.get().load(match.third).into(profilePicture)
        itemView.setOnClickListener {
            listener.onItemClicked(match)
        }
    }

    interface ContentListener {
        fun onItemClicked(match: Triple<String, String, Uri>) // implemented in MyMatchesFragment
    }
}