package com.thesis.exposureapp.ui.mix_and_match.matches

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.thesis.exposureapp.R

class MessageFragment : Fragment() {
    private val db: FirebaseFirestore = Firebase.firestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.match_profile_fragment, container, false)
        val profilePicture: ImageView = root.findViewById(R.id.profile_picture)
        val username: TextView = root.findViewById(R.id.username)
        val recipientText: TextView = root.findViewById(R.id.recipient)
        val subjectText: TextView = root.findViewById(R.id.subject)
        val messageText: TextView = root.findViewById(R.id.message)
        val match: Triple<String, String, Uri> = arguments?.getSerializable("match") as Triple<String, String, Uri>
        Picasso.get().load(match.third).into(profilePicture)
        username.text = match.second
        loadIntroAndEmailFromDB(match.first, root, recipientText)

        // send email
        val sendEmailButton: Button = root.findViewById(R.id.sendEmailButton)
        sendEmailButton.setOnClickListener {
            val recipient = recipientText.text.toString().trim()
            val subject = subjectText.text.toString().trim()
            val message = messageText.text.toString().trim()
            sendEmail(recipient, subject, message)
        }

        return root
    }

    private fun loadIntroAndEmailFromDB(matchId: String, root: View, recipientText: TextView) {
        val intro: TextView = root.findViewById(R.id.intro)
        db.collection("users").document(matchId).get()
            .addOnSuccessListener {
                intro.text = it.get("intro").toString()
                recipientText.text = it.get("email").toString()
            }.addOnFailureListener {
                intro.visibility = View.GONE
            }
    }

    private fun sendEmail(recipient: String, subject: String, message: String) {
        val mIntent = Intent(Intent.ACTION_SEND)
        mIntent.data = Uri.parse("mailto:")
        mIntent.type = "text/plain"
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        mIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        mIntent.putExtra(Intent.EXTRA_TEXT, message)
        try {
            startActivity(Intent.createChooser(mIntent, "Choose Email Client..."))
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }
}
