package com.thesis.exposureapp.forum


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.thesis.exposureapp.R
import com.thesis.exposureapp.comments.CommentAdapter
import com.thesis.exposureapp.models.Comment
import com.thesis.exposureapp.models.ForumThread
import com.thesis.exposureapp.models.User
import java.text.SimpleDateFormat
import java.util.*

class ThreadFragment : Fragment() {
    private var answersView: RecyclerView? = null // view of the list of answers
    private var answersList: List<Comment>? = ArrayList<Comment>() // all comments
    private lateinit var inputAnswer: EditText
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.thread_fragment, container, false)
        // question
        val ft: ForumThread = arguments?.getSerializable("ft") as ForumThread
        val questionView: TextView = root.findViewById(R.id.thread_question)
        questionView.text = ft.question
        // answers
        answersList = ft.answers
        answersView = root.findViewById(R.id.thread_answers)
        answersView?.setHasFixedSize(true)
        answersView?.layoutManager = LinearLayoutManager(context)
        answersView?.itemAnimator = DefaultItemAnimator()
        answersView?.adapter = answersList?.let { CommentAdapter(it) }
        // posting new answer
        inputAnswer = root.findViewById(R.id.input_answer)
        val postButton: Button = root.findViewById(R.id.post_button)
        postButton.setOnClickListener {
            ft.docId?.let { it1 -> postAnswerToThread(inputAnswer.text.toString(), it1) }
        }
        return root
    }

    @SuppressLint("SimpleDateFormat")
    private fun postAnswerToThread(answer: String, docId: String) {
        val db: FirebaseFirestore = Firebase.firestore
        val username = User.name + " " + User.surname
        val date: String = SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date()).toString()
        val newAnswer = hashMapOf(
            "answer" to answer,
            "date" to date,
            "username" to username
        )
        db.collection("threads").document(docId)
            .update("answers", FieldValue.arrayUnion(newAnswer))
            .addOnSuccessListener {
                inputAnswer.text.clear()
            }
            .addOnFailureListener {
                Log.d("Debug", "ops! Error writing document ", it)
            }
        //TODO: it would be nice to have a refresh here and update of adapter
    }
}