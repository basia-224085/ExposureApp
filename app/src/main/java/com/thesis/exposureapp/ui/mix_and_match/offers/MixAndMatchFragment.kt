package com.thesis.exposureapp.ui.mix_and_match.offers

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.thesis.exposureapp.Communicator
import com.thesis.exposureapp.R
import com.thesis.exposureapp.models.Offer
import com.thesis.exposureapp.models.User
import com.thesis.exposureapp.ui.mix_and_match.matches.MyMatchesFragment
import java.util.ArrayList

class MixAndMatchFragment : Fragment(), OfferViewHolder.ContentListener {
    private lateinit var comm: Communicator
    private val db: FirebaseFirestore = Firebase.firestore

    private lateinit var offersView: RecyclerView // view of the list of offers
    private var offerList: ArrayList<Offer> = ArrayList<Offer>() // all offers
    private val offerAdapter = OfferAdapter(offerList, this)
    private lateinit var lookForAMatchButton: Button
    private lateinit var chosenOffer: Offer
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.mix_and_match_fragment, container, false)
        comm = activity as Communicator
        val myMatchesButton: Button = root.findViewById(R.id.my_matches_button)
        myMatchesButton.setOnClickListener {
            comm.replaceFragment(fragment = MyMatchesFragment())
        }
        val addOfferButton: Button = root.findViewById(R.id.add_offer_button)
        addOfferButton.setOnClickListener {
            //directToCorrectForm()
           //comm.passAddInfoToForm("mentee", "add_offer") // proceed as mentee
            comm.passInfoToForm("add_offer", null)
        }
        lookForAMatchButton = root.findViewById(R.id.look_for_a_match_button)
        lookForAMatchButton.isEnabled = false // enabled only when offer is chosen
        lookForAMatchButton.setOnClickListener {
            comm.passOffer(chosenOffer)
        }
        // list of user's offers
        offersView = root.findViewById(R.id.offers)
        offersView.setHasFixedSize(true)
        offersView.layoutManager = LinearLayoutManager(context)
        offersView.itemAnimator = DefaultItemAnimator()
        getOffersFromDB()
        return root
    }

    private fun getOffersFromDB() {
        // get all users's offers from db (they will either be for models or photographers)
        db.collection("offers")
            .whereEqualTo("uid", User.id)
            .get().addOnSuccessListener {
                offerList.clear()
                for (doc in it) {
                    val offer = doc.toObject(Offer::class.java)
                    offer.docId = doc.id
                    offerList.add(offer)
                }
                offersView.adapter = offerAdapter
            }
    }


    /*private fun directToCorrectForm() {
        if (User.role.compareTo(getString(R.string.entrepreneur)) == 0) {
            comm.passAddInfoToForm("mentor", "add_offer") // proceed as mentor
        } else {
            comm.passAddInfoToForm("mentee", "add_offer") // proceed as mentee
        }
    }*/

    override fun onItemClicked(clickedOffer: Offer) {
        lookForAMatchButton.isEnabled = true
        chosenOffer = clickedOffer
        //TODO("Not yet implemented") a co tu mialam implementowac dalej? te kolory chyba tylko
    }

    override fun onItemDeleted(deletedOffer: Offer) {
        deletedOffer.docId?.let { it ->
            db.collection("offers").document(it).delete()
                .addOnSuccessListener {
                    comm.replaceFragment(fragment = MixAndMatchFragment())
                }
                .addOnFailureListener {
                    Log.d("Debug", "ops! $it")
                }
        }
    }

    override fun onItemEdited(editedOffer: Offer) {
        //todo chyba tam niepotrzebne troche jest
       // comm.passEditInfoToForm(editedOffer.offerRecipient, "edit_offer", editedOffer)
        comm.passInfoToForm("edit_offer", editedOffer)
    }
}