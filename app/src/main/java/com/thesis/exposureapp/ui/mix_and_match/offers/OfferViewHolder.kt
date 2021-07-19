package com.thesis.exposureapp.ui.mix_and_match.offers

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.thesis.exposureapp.R
import com.thesis.exposureapp.models.Offer

class OfferViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.offer_adapter, parent, false)) {
    private var offerName: TextView = itemView.findViewById(R.id.offer_name)
    private var editButton: ImageView = itemView.findViewById(R.id.edit_offer)
    private var deleteButton: ImageView = itemView.findViewById(R.id.delete_offer)
    fun bind(offer: Offer, listener: ContentListener) {
        offerName.text = offer.offerName
        itemView.setOnClickListener {
            it.setBackgroundColor(
                ContextCompat.getColor(it.context,
                R.color.powder_blue))
            listener.onItemClicked(offer)
        }
        editButton.setOnClickListener {
            listener.onItemEdited(offer)
        }
        deleteButton.setOnClickListener {
            listener.onItemDeleted(offer)
        }
    }

    interface ContentListener {
        fun onItemClicked(clickedOffer: Offer) // implemented in MixAndMatchFragment
        fun onItemDeleted(deletedOffer: Offer)
        fun onItemEdited(editedOffer: Offer)
    }
}