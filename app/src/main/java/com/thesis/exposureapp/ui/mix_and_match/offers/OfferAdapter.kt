package com.thesis.exposureapp.ui.mix_and_match.offers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thesis.exposureapp.models.Offer
import java.util.*


class OfferAdapter constructor(
    private val list: ArrayList<Offer>,
    private val listener: OfferViewHolder.ContentListener
) : RecyclerView.Adapter<OfferViewHolder>() {
    private var selected: Int = -1
    private lateinit var myParent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        myParent = parent
        return OfferViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        val offer: Offer = list[position]
        holder.bind(offer, listener)
        /*holder.itemView.setBackgroundColor(ContextCompat.getColor(myParent.context,
            R.color.honeydew))
        if (selected == position) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(myParent.context,
                R.color.powder_blue))
        }
        holder.itemView.setOnClickListener {
             val previousItem: Int = selected
             selected = position
             notifyItemChanged(previousItem)
             notifyItemChanged(position)
         }*/
    }

    override fun getItemCount(): Int = list.size
}

