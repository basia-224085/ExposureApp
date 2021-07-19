package com.thesis.exposureapp.ui.mix_and_match.matches

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.thesis.exposureapp.Communicator
import com.thesis.exposureapp.R
import com.thesis.exposureapp.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.ArrayList

class MyMatchesFragment : Fragment(), MatchViewHolder.ContentListener {
    private lateinit var matchesView: RecyclerView // view of the list of matches
    private var matchesList: ArrayList<Triple<String, String, Uri>> = ArrayList<Triple<String, String, Uri>>() // all matches
    private val matchAdapter = MatchAdapter(matchesList, this)
    private lateinit var comm: Communicator
    private val db: FirebaseFirestore = Firebase.firestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.my_matches_fragment, container, false)
        comm = activity as Communicator
        matchesView = root.findViewById(R.id.my_matches)
        matchesView.setHasFixedSize(true)
        matchesView.layoutManager = LinearLayoutManager(context)
        matchesView.itemAnimator = DefaultItemAnimator()
        CoroutineScope(Dispatchers.IO).launch {
            loadMatches()
        }
        //search
        val searchEngine: SearchView = root.findViewById(R.id.search_match)
        searchEngine.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                matchAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                matchAdapter.filter.filter(query)
                return false
            }
        })
        return root
    }

    // coroutines on one thread:
    private suspend fun loadMatches() {
        val idsList = getMatchesIdsFromDB()
        matchesList.clear()
        for (id in idsList) {
            getMatchInfoFromDB(id)
        }
        withContext(Dispatchers.Main) {
            matchesView.adapter = matchAdapter
        }
    }

    private suspend fun getMatchesIdsFromDB(): ArrayList<String> {
        val idsList: ArrayList<String> = ArrayList()
        try {
            val data = db.collection("matches").get().await()
            if (User.role == resources.getString(R.string.photographer)) {
                for (document in data) {
                    if ((document.get("isMatched").toString() == "yes") &&
                        (document.get("photographerId").toString() == User.id)
                    ) {
                        idsList.add(document.get("modelId").toString())
                    }
                }
            } else if (User.role == resources.getString(R.string.model)) {
                for (document in data) {
                    if ((document.get("isMatched").toString() == "yes") &&
                        (document.get("modelId").toString() == User.id)
                    ) {
                        idsList.add(document.get("photographerId").toString())
                    }
                }
            }
            return idsList
        } catch (e: Exception) {
            Log.d("Debug", "ops! Exception: $e")
            return idsList
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun getMatchInfoFromDB(matchId: String) {
        try {
            val data = db.collection("users").document(matchId).get().await()
            val nameAndSurname: String = data.get("name").toString() + " " + data.get("surname").toString()
            try {
                val profileReference: StorageReference =
                    FirebaseStorage.getInstance().reference.child("users/${matchId}/profile_picture.jpg")
                val uri = profileReference.downloadUrl.await()
                matchesList.add(Triple(matchId, nameAndSurname, uri))
            } catch (e: Exception) {
                // if there is no profile pic, use default one
                val defaultReference = FirebaseStorage.getInstance().reference.child("basic/user_profile_picture.png")
                val uri = defaultReference.downloadUrl.await()
                matchesList.add(Triple(matchId, nameAndSurname, uri))
            }
        } catch (e: Exception) {
            Log.d("Debug", "ops! Exception: $e")
        }
    }

    override fun onItemClicked(match: Triple<String, String, Uri>) {
        comm.passMatchInfo(match)
    }
}
