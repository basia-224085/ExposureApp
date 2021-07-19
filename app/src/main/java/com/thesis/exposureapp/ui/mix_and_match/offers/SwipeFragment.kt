package com.thesis.exposureapp.ui.mix_and_match.offers

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bnsos.wmnness.MainActivity
import com.bnsos.wmnness.R
import com.bnsos.wmnness.models.User
import com.bnsos.wmnness.models.offers.MenteeOffer
import com.bnsos.wmnness.models.offers.MentorOffer
import com.bnsos.wmnness.models.offers.Offer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.tasks.await
import java.util.*


class SwipeFragment : Fragment() {
    private val db: FirebaseFirestore = Firebase.firestore
    private var offerList: ArrayList<Offer> = ArrayList<Offer>() // all offers
    private var chosenOfferDocId: String? = null
    private lateinit var matchLayout: LinearLayout
    private lateinit var loadingLayout: TextView
    private var viewedOfferIndex = 0
    private var amountOfTopOffers = 20
    private var shouldContinue = true

    // for notification:
    private val CHANNEL_ID = "channel_id_01"
    private val notificationId = 101

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.swipe_frag, container, false)
        // set visibility to correct layout
        matchLayout = root.findViewById(R.id.match)
        matchLayout.visibility = View.INVISIBLE
        loadingLayout = root.findViewById(R.id.loading)
        loadingLayout.visibility = View.VISIBLE
        //sendNotification("test")

        if (User.role == resources.getString(R.string.entrepreneur)) {
            val chosenOffer: MentorOffer = arguments?.getSerializable("offer") as MentorOffer
            chosenOfferDocId = chosenOffer.docId
            CoroutineScope(IO).launch {
                getMatches("mentee", "mentor", chosenOffer, root)
            }
        } else {
            val chosenOffer: MenteeOffer = arguments?.getSerializable("offer") as MenteeOffer
            chosenOfferDocId = chosenOffer.docId
            CoroutineScope(IO).launch {
                getMatches("mentor", "mentee", chosenOffer, root)
            }
        }
        return root
    }

    // coroutines on one thread:
    private suspend fun getMatches(wantedType: String, type: String, chosenOffer: Offer, root: View) {
        getOffersFromDB(wantedType)
        removeAlreadyMatched(type)
        if (type == "mentor") { // calculate compatibility for each offer
            for (offer in offerList) {
                offer.compatibility = compatibility(chosenOffer as MentorOffer, offer as MenteeOffer, type)
            }
        } else {
            for (offer in offerList) {
                offer.compatibility = compatibility(offer as MentorOffer, chosenOffer as MenteeOffer, type)
            }
        }
        // show offers recurrently
        offerList.sortBy { it.compatibility }
        offerList.reverse()
        if (offerList.size < amountOfTopOffers) amountOfTopOffers = offerList.size
        setOfferOnMainThread(type, root, offerList[viewedOfferIndex])
    }

    private suspend fun getOffersFromDB(wantedType: String) {
        try {
            offerList.clear()
            val data = db.collection("offers")
                .whereEqualTo("offerType", wantedType)
                .get().await()
            if (wantedType == "mentor") {
                for (doc in data) {
                    val mentorOffer = doc.toObject<MentorOffer>()
                    mentorOffer.docId = doc.id
                    offerList.add(mentorOffer)
                }
            } else {
                for (doc in data) {
                    val menteeOffer = doc.toObject<MenteeOffer>()
                    menteeOffer.docId = doc.id
                    offerList.add(menteeOffer)
                }
            }
        } catch (e: Exception) {
            Log.d("Debug", "ops! Exception: $e")
        }
    }

    private suspend fun removeAlreadyMatched(type: String) {
        try {
            val indexes: ArrayList<Int> = ArrayList() // indexes for removal
            if (type == "mentor") {
                offerList.forEachIndexed { index, offer ->
                    val match = db.collection("matches")
                        .whereEqualTo("mentorOfferId", chosenOfferDocId)
                        .whereEqualTo("menteeOfferId", offer.docId)
                        .whereEqualTo("isMatched", "yes")
                        .get().await()
                    if (!match.isEmpty) indexes.add(index)
                }
            } else {
                offerList.forEachIndexed { index, offer ->
                    val match = db.collection("matches")
                        .whereEqualTo("mentorOfferId", offer.docId)
                        .whereEqualTo("menteeOfferId", chosenOfferDocId)
                        .whereEqualTo("isMatched", "yes")
                        .get().await()
                    if (!match.isEmpty) indexes.add(index)
                }
            }
            indexes.asReversed().forEach {
                offerList.removeAt(it) // remove offers already matched
            }
        } catch (e: Exception) {
            Log.d("Debug", "ops! Exception: $e")
        }
    }

    private fun compatibility(mentor: MentorOffer, mentee: MenteeOffer, type: String): Float {
        val compatibility: Float
        var points = 0.0f
        if (mentor.mentorLocation == mentee.menteeLocation) points++
        languageLoop@ for (language in mentor.mentorLanguages) {
            for (language2 in mentee.menteeLanguages) {
                if (language == language2) {
                    points++
                    break@languageLoop
                }
            }
        }
        if (type == "mentor") {
            if (mentor.menteeProfession == mentee.menteeProfession) points++
            if (mentee.menteeAge in mentor.menteeMinAge..mentor.menteeMaxAge) points++
            for (trait in mentor.menteeTraits) {
                for (trait2 in mentee.menteeTraits) {
                    if (trait == trait2) {
                        points++
                        break
                    }
                }
            }
            if (mentee.menteeAboutMe != "") points++
            if (mentee.menteeVisionOfCompany != "") points++
            val pointsMAX = 12
            compatibility = (points / pointsMAX) * 100
        } else {
            if (mentee.mentorProfession == mentor.mentorProfession) points++
            if (mentor.mentorYearsOfExperience >= mentee.mentorMinYearsOfExperience) points++
            if (mentor.mentorAge in mentee.mentorMinAge..mentee.mentorMaxAge) points++
            for (trait in mentor.mentorTraits) {
                for (trait2 in mentee.mentorTraits) {
                    if (trait == trait2) {
                        points++
                        break
                    }
                }
            }
            if (mentor.mentorAboutMe != "") points++
            if (mentor.mentorAboutMyCompany != "") points++
            val pointsMAX = 13.0f
            compatibility = (points / pointsMAX) * 100
        }
        return compatibility
    }

    private suspend fun setOfferOnMainThread(type: String, root: View, offer: Offer) {
        withContext(Main) {
            loadOffer(root, type, offer)
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun loadOffer(root: View, type: String, offer: Offer) {
        val authorInfo: Pair<String, Uri?>
        withContext(IO) {
            authorInfo = getAuthorInfoFromDB(offer) // name, surname and profile picture
        }
        val profilePicture: ImageView = root.findViewById(R.id.profile_picture)
        val username: TextView = root.findViewById(R.id.username)
        val age: TextView = root.findViewById(R.id.age)
        val languages: TextView = root.findViewById(R.id.languages)
        val location: TextView = root.findViewById(R.id.location)
        val traits: TextView = root.findViewById(R.id.traits)
        val aboutMe: TextView = root.findViewById(R.id.about_me)
        val profession: TextView = root.findViewById(R.id.profession)
        val experience: TextView = root.findViewById(R.id.years_of_experience)
        val aboutCompany: TextView = root.findViewById(R.id.about_company)
        val compatibility: TextView = root.findViewById(R.id.compatibility)

        Picasso.get().load(authorInfo.second).into(profilePicture)
        username.text = authorInfo.first
        compatibility.text = "${offer.compatibility?.toInt()}% compatibility"
        val sb: StringBuilder = java.lang.StringBuilder()

        if (type == "mentor") { // shows mentees
            val displayedOffer: MenteeOffer = offer as MenteeOffer
            age.text = "${displayedOffer.menteeAge} years old"
            sb.append("| ")
            for (lan in displayedOffer.menteeLanguages) {
                sb.append(lan).append(" | ")
            }
            languages.text = "Speaks:  $sb"
            location.text = "Lives in:  ${displayedOffer.menteeLocation}"
            sb.clear()
            sb.append("| ")
            for (trait in displayedOffer.menteeTraits) {
                sb.append(trait).append(" | ")
            }
            traits.text = "Traits:  ${displayedOffer.menteeTraits}"
            aboutMe.text = "About me:  ${displayedOffer.menteeAboutMe}"
            profession.text = "Profession:  ${displayedOffer.menteeProfession}"
            experience.visibility = View.GONE
            aboutCompany.text = "About my company: ${displayedOffer.menteeVisionOfCompany}"
        } else { // shows mentors
            val displayedOffer: MentorOffer = offer as MentorOffer
            age.text = "${displayedOffer.mentorAge} years old"
            sb.append("| ")

            for (lan in displayedOffer.mentorLanguages) {
                sb.append(lan).append(" | ")
            }
            languages.text = "Speaks:  $sb"
            location.text = "Lives in:  ${displayedOffer.mentorLocation}"
            sb.clear()
            for (trait in displayedOffer.mentorTraits) {
                sb.append("- ").append(trait).append("\n")
            }
            traits.text = "Traits:\n$sb"
           // aboutMe.text = "About me:  ${displayedOffer.mentorAboutMe}"
            aboutMe.visibility = View.GONE
            profession.text = "Profession:  ${displayedOffer.mentorProfession}"
            experience.text = "Years of experience: ${displayedOffer.mentorYearsOfExperience}"
        //aboutCompany.text = "About my company: ${displayedOffer.mentorAboutMyCompany}"
            aboutCompany.visibility = View.GONE

        }
        switchLayouts()

        // buttons (update matches collection and go to next offer)
        val acceptButton: Button = root.findViewById(R.id.accept_button)
        acceptButton.setOnClickListener {
            viewedOfferIndex++
            CoroutineScope(Main).launch {
                updateMatchesInDB(type)
                Log.d("DEBUG", "test: $shouldContinue")
                switchLayouts()
                if (shouldContinue) {
                    Log.d("DEBUG", "test: hello12")
                    setOfferOnMainThread(type, root, offerList[viewedOfferIndex])
                }
            }
        }
        val discardButton: Button = root.findViewById(R.id.discard_button)
        discardButton.setOnClickListener {
            viewedOfferIndex++
            switchLayouts()
            if (shouldContinue) {
                CoroutineScope(Main).launch {
                    setOfferOnMainThread(type, root, offerList[viewedOfferIndex])
                }
            }
        }
    }

    private suspend fun getAuthorInfoFromDB(offer: Offer): Pair<String, Uri?> {
        val storageReference = FirebaseStorage.getInstance().reference
        return try {
            val doc = db.collection("users")
                .document(offer.uid)
                .get().await()
            try {
                val profileReference: StorageReference =
                    storageReference.child("users/${offer.uid}/profile_picture.jpg")
                val pic = profileReference.downloadUrl.await()
                Pair("${doc.get("name")} ${doc.get("surname")}", pic)
            } catch (e: Exception) {
                val defaultReference = storageReference.child("basic/user_profile_picture.png")
                val pic = defaultReference.downloadUrl.await()
                Pair("${doc.get("name")} ${doc.get("surname")}", pic)
            }
        } catch (e: Exception) {
            Log.d("Debug", "ops! Exception: $e")
            Pair("", null)
        }
    }

    private suspend fun updateMatchesInDB(type: String) {
        val mentorDocId: String
        val menteeDocId: String
        val mentorId: String
        val menteeId: String

        try {
            if (type == "mentor") {
                mentorDocId = chosenOfferDocId.toString()
                menteeDocId = offerList[viewedOfferIndex].docId.toString()
                mentorId = User.id
                menteeId = offerList[viewedOfferIndex].uid
            } else {
                mentorDocId = offerList[viewedOfferIndex].docId.toString()
                menteeDocId = chosenOfferDocId.toString()
                mentorId = offerList[viewedOfferIndex].uid
                menteeId = User.id
            }
            Log.d("DEBUG", "test: hello1")
            val match = db.collection("matches")
                .whereEqualTo("mentorOfferId", mentorDocId)
                .whereEqualTo("menteeOfferId", menteeDocId)
                .get().await()
            Log.d("DEBUG", "test: hello")
            Log.d("DEBUG", "test: ${match.isEmpty}")
            if (match.isEmpty) {
                val newMatch = hashMapOf(
                    "mentorOfferId" to mentorDocId,
                    "menteeOfferId" to menteeDocId,
                    "mentorId" to mentorId,
                    "menteeId" to menteeId,
                    "isMatched" to "no"
                )
                try {
                    db.collection("matches").add(newMatch)
                } catch (e: Exception) {
                    Log.d("Debug", "ops! Exception: $e")
                }
            } else {
                for (doc in match) {
                    sendNotification(offerList[viewedOfferIndex].uid)
                    val newMatch = mapOf("isMatched" to "yes")
                    try {
                        db.collection("matches").document(doc.id).update(newMatch)
                    } catch (e: Exception) {
                        Log.d("Debug", "ops! Exception: $e")
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("Debug", "ops! Exception: $e")
        }
    }

    private fun switchLayouts() {
        if (viewedOfferIndex < amountOfTopOffers) {
            if (loadingLayout.isVisible) {
                loadingLayout.visibility = View.INVISIBLE
                matchLayout.visibility = View.VISIBLE
            } else {
                loadingLayout.visibility = View.VISIBLE
                matchLayout.visibility = View.INVISIBLE
            }
        } else {
            shouldContinue = false
            matchLayout.visibility = View.INVISIBLE
            loadingLayout.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            loadingLayout.text = "Currently there is no more matches for you"
            loadingLayout.visibility = View.VISIBLE
        }
    }

    private fun sendNotification(user1Id: String) {
        // TODO: not really used right now
        createNotificationChannel()
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, 0)
        //val pendingIntent = NavDeepLinkBuilder(requireContext())
            /*.setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.my_matches)
            .createPendingIntent()*/
        val bitmapLargeIcon = BitmapFactory.decodeResource(requireContext().resources, R.drawable.logo_shoe)


        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.mix_and_match)
            .setContentTitle("New Match!")
            .setContentText("We have found you a new match. Check it out and don't hesitate to get in contact")
            .setLargeIcon(bitmapLargeIcon)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(requireContext())) {
            notify(notificationId, builder.build())
        }

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Title"
            val descriptionText = "This is description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}