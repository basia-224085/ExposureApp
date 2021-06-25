package com.thesis.exposureapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.thesis.exposureapp.authentication.AuthenticateActivity
import com.thesis.exposureapp.models.User

class MainActivity: AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) { // go to authentication page if no user is logged in
            startActivity(Intent(this, AuthenticateActivity::class.java))
        }
        setContentView(R.layout.main_activity)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //init User Singleton
        initUserSingleton()

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.itemIconTintList = null
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_forum,
                R.id.nav_user_profile
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun initUserSingleton() {
        val db: FirebaseFirestore = Firebase.firestore
        val uId: String? = FirebaseAuth.getInstance().currentUser?.uid

        val uDocument = uId?.let { db.collection("users").document(it) }
        uDocument?.get()?.addOnSuccessListener { document ->
            if (document != null) {
                // fill fragment view with data from database
                User.id = uId
                User.name = document.get("name").toString()
                User.surname = document.get("surname").toString()
                User.role = document.get("role").toString()
                User.intro = document.get("intro").toString()
                User.email = document.get("email").toString()
            } else {
                Log.d("Debug", "ops! No such document")
            }
        }?.addOnFailureListener { exception ->
            Log.d("Debug", "ops! get failed with ", exception)
        }
    }
}
