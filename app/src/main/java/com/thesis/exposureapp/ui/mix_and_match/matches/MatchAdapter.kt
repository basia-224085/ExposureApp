package com.thesis.exposureapp.ui.mix_and_match.matches

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class MatchAdapter constructor(
    private val list: ArrayList<Triple<String, String, Uri>>,
    private val listener: MatchViewHolder.ContentListener
) : RecyclerView.Adapter<MatchViewHolder>(), Filterable {
    var filteredList = ArrayList<Triple<String, String, Uri>>()

    init {
        filteredList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MatchViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match: Triple<String, String, Uri> = filteredList[position]
        holder.bind(match, listener)
    }

    override fun getItemCount(): Int = filteredList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                filteredList = if (charSearch.isEmpty()) {
                    list // nothing typed in search engine
                } else {
                    val resultList = ArrayList<Triple<String, String, Uri>>()
                    for (row in list) {
                        if (row.second.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))
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
                filteredList = results?.values as ArrayList<Triple<String, String, Uri>>
                notifyDataSetChanged()
            }
        }
    }
}
