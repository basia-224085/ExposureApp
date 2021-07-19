package com.thesis.exposureapp.ui.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.thesis.exposureapp.R
import com.thesis.exposureapp.models.Post


private const val VIEW_TYPE_LOADING = 0
private const val VIEW_TYPE_ARTICLE = 1
private const val VIEW_TYPE_INTERVIEW = 2
private const val VIEW_TYPE_AD = 3

class PostAdapter(var list: List<Post?>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ARTICLE -> {
                val view: View =
                    LayoutInflater.from(parent.context).inflate(R.layout.article, parent, false)
                ArticleViewHolder(view)
            }
            VIEW_TYPE_INTERVIEW -> {
                val view: View =
                    LayoutInflater.from(parent.context).inflate(R.layout.interview, parent, false)
                InterviewViewHolder(view)
            }
            VIEW_TYPE_AD -> {
                val view: View =
                    LayoutInflater.from(parent.context).inflate(R.layout.ad, parent, false)
                AdViewHolder(view)
            }
            else -> {
                val view: View =
                    LayoutInflater.from(parent.context).inflate(R.layout.loading, parent, false)
                LoadingViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            is ArticleViewHolder -> {
                showArticleView(viewHolder, position)
            }
            is InterviewViewHolder -> {
                showInterviewView(viewHolder, position)
            }
            is AdViewHolder -> {
                showAdView(viewHolder, position)
            }
            is LoadingViewHolder -> {
                showLoadingView(viewHolder, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (list == null) 0 else list!!.size
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            list!![position] == null -> VIEW_TYPE_LOADING
            list!![position]?.content?.get("type").toString() == "article" -> VIEW_TYPE_ARTICLE
            list!![position]?.content?.get("type").toString() == "interview" -> VIEW_TYPE_INTERVIEW
            else -> VIEW_TYPE_AD
        }
    }

    private inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.title)
        var subtitle: TextView = itemView.findViewById(R.id.subtitle)
        var text: TextView = itemView.findViewById(R.id.text)
    }

    private inner class InterviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.title)
        var subtitle: TextView = itemView.findViewById(R.id.subtitle)
        var text: TextView = itemView.findViewById(R.id.text)
        var picture: ImageView = itemView.findViewById(R.id.picture)
    }

    private inner class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.title)
        var subtitle: TextView = itemView.findViewById(R.id.subtitle)
        var text: TextView = itemView.findViewById(R.id.text)
        var video: VideoView = itemView.findViewById(R.id.video)
        var playButton: ImageView = itemView.findViewById(R.id.play_button)
    }

    private inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
    }

    private fun showLoadingView(viewHolder: LoadingViewHolder, position: Int) {
        //ProgressBar would be displayed
    }

    private fun showArticleView(viewHolder: ArticleViewHolder, position: Int) {
        val item = list!![position]
        viewHolder.title.text = item?.content?.get("title")
        viewHolder.subtitle.text = item?.content?.get("subtitle")
        viewHolder.text.text = item?.content?.get("text")
    }

    private fun showInterviewView(viewHolder: InterviewViewHolder, position: Int) {
        val item = list!![position]
        viewHolder.title.text = item?.content?.get("title")
        viewHolder.subtitle.text = item?.content?.get("subtitle")
        viewHolder.text.text = item?.content?.get("text")
        Picasso.get().load(item?.content?.get("picture")).into(viewHolder.picture)
    }

    private fun showAdView(viewHolder: AdViewHolder, position: Int) {
        val item = list!![position]
        viewHolder.title.text = item?.content?.get("title")
        viewHolder.subtitle.text = item?.content?.get("subtitle")
        viewHolder.text.text = item?.content?.get("text")
        viewHolder.video.setVideoURI(Uri.parse(item?.content?.get("video")))
        val mc = MediaController(viewHolder.video.context)
        viewHolder.video.setMediaController(mc)

        viewHolder.video.setOnClickListener {
            viewHolder.playButton.visibility = View.GONE
        }

    }
}