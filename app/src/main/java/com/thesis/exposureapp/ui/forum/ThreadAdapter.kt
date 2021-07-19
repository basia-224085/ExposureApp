package com.thesis.exposureapp.ui.forum


import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.thesis.exposureapp.models.ForumThread
import java.util.*
import kotlin.collections.ArrayList

class ThreadAdapter constructor(
    private val list: ArrayList<ForumThread>,
    private val listener: ThreadViewHolder.ContentListener
) : RecyclerView.Adapter<ThreadViewHolder>(), Filterable {
    var filteredList = ArrayList<ForumThread>()

    init {
        filteredList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreadViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ThreadViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ThreadViewHolder, position: Int) {
        val forumThread: ForumThread = filteredList[position]
        holder.bind(forumThread, listener)
    }

    override fun getItemCount(): Int = filteredList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                filteredList = if (charSearch.isEmpty()) {
                    list // nothing typed in search engine
                } else {
                    val resultList = ArrayList<ForumThread>()
                    for (row in list) {
                        if (row.question?.toLowerCase(Locale.ROOT)
                                ?.contains(charSearch.toLowerCase(Locale.ROOT)) == true
                        ) {
                            resultList.add(row)
                        }
                    }
                    resultList // all matching results
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as ArrayList<ForumThread>
                notifyDataSetChanged()
            }

        }
    }
}
