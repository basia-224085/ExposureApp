package com.thesis.exposureapp.ui.authentication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.thesis.exposureapp.MainActivity
import com.thesis.exposureapp.R

class RegisterFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private var name: EditText? = null
    private var surname: EditText? = null
    private var email: EditText? = null
    private var password: EditText? = null
    private var confirmPassword: EditText? = null
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var roleSwitch: Switch
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.register_fragment, container, false)
        auth = FirebaseAuth.getInstance()
        name = root.findViewById(R.id.name)
        surname = root.findViewById(R.id.surname)
        email = root.findViewById(R.id.email)
        password = root.findViewById(R.id.password)
        confirmPassword = root.findViewById(R.id.confirm_password)
        roleSwitch = root.findViewById(R.id.role)

        // todo: change for toggle button
        roleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                roleSwitch.text = "I am a photographer"
            } else {
                roleSwitch.text = "I am a model"
            }
        }

        val registerBtn: Button = root.findViewById(R.id.register_button)
        registerBtn.setOnClickListener {
            registerNewUser()
        }
        return root
    }
    private fun registerNewUser() {
        val uName: String = name?.text.toString().trim()
        val uSurname: String = surname?.text.toString().trim()
        val uEmail: String = email?.text.toString().trim()
        val uPassword: String = password?.text.toString().trim()
        val uConfirmPassword: String = confirmPassword?.text.toString().trim()

        if (TextUtils.isEmpty(uName) || TextUtils.isEmpty(uSurname) ||
            TextUtils.isEmpty(uEmail) || TextUtils.isEmpty(uPassword) ||
            TextUtils.isEmpty(uConfirmPassword)
        ) {
            toast("Fill all boxes")
            return
        }
        if (!uName.onlyLetters() || !uSurname.onlyLetters()) {
            toast("Name and surname must contain only letters")
            return
        }
        if (uPassword.length < 6) {
            toast("Password has to have at least 6 characters")
            return
        }
        if (uPassword != uConfirmPassword) {
            toast("Passwords do not match")
            return
        }
        auth.createUserWithEmailAndPassword(uEmail, uPassword).addOnCompleteListener {
            if (!it.isSuccessful) {
                toast("ops! Authentication failed" + it.exception)
            } else {
                //create new User and add to the database
                val uRole: String = if (roleSwitch.isChecked) {
                    getString(R.string.photographer)
                } else {
                    getString(R.string.model)
                }
                Log.d("debug", "ops! $uEmail")

                val newUser = hashMapOf(
                    "name" to uName,
                    "surname" to uSurname,
                    "role" to uRole,
                    "email" to uEmail,
                    "intro" to ""
                )
                val db: FirebaseFirestore = Firebase.firestore
                auth.currentUser?.let { it1 ->
                    db.collection("users").document(it1.uid)
                        .set(newUser)
                        .addOnSuccessListener {
                            Log.d(
                                "Debug",
                                "ops! DocumentSnapshot successfully written!"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.d(
                                "Debug",
                                "ops! Error writing document",
                                e
                            )
                        }
                }
                startActivity(Intent(context, MainActivity::class.java))
            }
        }
    }

    @Override
    override fun onResume() {
        super.onResume()
    }

    private fun toast(toast_text: String) {
        Toast.makeText(context, toast_text, Toast.LENGTH_SHORT).show()
    }

    private fun String.onlyLetters() = all { it.isLetter() }
}