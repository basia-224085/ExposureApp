package com.thesis.exposureapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.thesis.exposureapp.R
import com.thesis.exposureapp.models.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.*

private const val NUMBER_OF_POSTS = 3

class HomeFragment : Fragment() {
    private lateinit var postsView: RecyclerView // view of the list of all posts
    private var postList = ArrayList<Post?>() // all posts
    private lateinit var postAdapter: PostAdapter
    private var isLoading = false
    private val db: FirebaseFirestore = Firebase.firestore


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.home_fragment, container, false)
        postsView = root.findViewById(R.id.posts)
        postList.clear()
        CoroutineScope(Main).launch {
            getPostsFromDB(0, NUMBER_OF_POSTS) // get first posts
            initAdapter()
            initScrollListener()
        }
        return root
    }

    private suspend fun getPostsFromDB(currentPosition: Int, numberOfPosts: Int) {
        try {
            val data = db.collection("posts").orderBy("postNumber")
                .whereGreaterThan("postNumber", currentPosition).limit(numberOfPosts.toLong())
                .get().await()
            for (document in data) {
                val post = document.toObject<Post>()
                postList.add(post)
            }
        } catch (e: Exception) {
            Log.d("DEBUG", "Error getting documents: ", e)
        }
    }

    private fun initAdapter() {
        postAdapter = PostAdapter(postList)
        postsView.adapter = postAdapter
    }

    private fun initScrollListener() {
        postsView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(postsView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(postsView, dx, dy)
                val linearLayoutManager = postsView.layoutManager as LinearLayoutManager?
                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == postList.size - 1) {
                        loadNextPostsFromDB() // bottom of the list
                        isLoading = true
                    }
                }
            }
        })
    }

    private fun loadNextPostsFromDB() {
        postList.add(null)
        postAdapter.notifyItemInserted(postList.size - 1)
        CoroutineScope(Main).launch {
            delay(2000)
            postList.removeAt(postList.size - 1)
            val scrollPosition = postList.size
            postAdapter.notifyItemRemoved(scrollPosition)
            getPostsFromDB(scrollPosition, NUMBER_OF_POSTS)
            postAdapter.notifyDataSetChanged()
            isLoading = false
        }
    }
}

