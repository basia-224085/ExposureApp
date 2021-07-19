package com.thesis.exposureapp.ui.forum

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.thesis.exposureapp.Communicator
import com.thesis.exposureapp.R
import com.thesis.exposureapp.models.ForumThread
import java.util.*

class ForumFragment : Fragment(), ThreadViewHolder.ContentListener {
    private lateinit var threadsView: RecyclerView // view of the list of threads
    private lateinit var addThread: FloatingActionButton
    private var threadList: ArrayList<ForumThread> = ArrayList<ForumThread>() // all threads
    private val threadAdapter = ThreadAdapter(threadList, this)
    private lateinit var comm: Communicator
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.forum_fragment, container, false)
        comm = activity as Communicator
        addThread = root.findViewById(R.id.add_thread)
        addThread.setOnClickListener {
            popUpAddThread()
        }
        threadsView = root.findViewById(R.id.threads)
        threadsView.setHasFixedSize(true)
        threadsView.layoutManager = LinearLayoutManager(context)
        threadsView.itemAnimator = DefaultItemAnimator()
        getThreadsFromDB()

        //search
        val searchEngine: SearchView = root.findViewById(R.id.search_thread)
        searchEngine.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                threadAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                threadAdapter.filter.filter(query)
                return false
            }
        })
        return root
    }

    private fun getThreadsFromDB() {
        val db: FirebaseFirestore = Firebase.firestore
        db.collection("threads")
            .get()
            .addOnSuccessListener {
                threadList.clear()
                for (document in it) {
                    val forumThread = document.toObject(ForumThread::class.java)
                    forumThread.docId = document.id
                    threadList.add(forumThread)
                }
                threadsView.adapter = threadAdapter
            }
            .addOnFailureListener {
                Log.d("DEBUG", "Error getting documents: ", it)
            }
    }

    @SuppressLint("ResourceType")
    override fun onItemClicked(clickedThread: ForumThread) {
        comm.passForumThread(clickedThread) // pass thread to MainActivity
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode", "SetTextI18n")
    private fun popUpAddThread() {
        val dialogBuilder: AlertDialog = AlertDialog.Builder(requireContext()).create()
        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.popup_text_editor, null)
        val instruction: TextView = dialogView.findViewById(R.id.optional_instruction)
        instruction.text = "New thread:"
        val textToEdit: EditText = dialogView.findViewById(R.id.text_to_edit)

        val acceptBtn: ImageView = dialogView.findViewById(R.id.accept_changes)
        acceptBtn.setOnClickListener {
            val newThread = hashMapOf(
                "question" to textToEdit.text.toString()
            )
            val db: FirebaseFirestore = Firebase.firestore
            db.collection("threads").add(newThread).addOnFailureListener {
                Log.d("Debug", "ops! Error writing document ", it)
            }
            dialogBuilder.dismiss()
        }
        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
    }
}