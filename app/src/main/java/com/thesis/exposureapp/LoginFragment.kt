package com.thesis.exposureapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var uEmail: EditText
    private lateinit var uPassword: EditText
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.login_fragment, container, false)

        auth = FirebaseAuth.getInstance()
        uEmail = root.findViewById(R.id.email)
        uPassword = root.findViewById(R.id.password)

        val loginBtn: Button = root.findViewById(R.id.login_button)
        loginBtn.setOnClickListener {
            logInUser()
        }

        return root
    }

    private fun logInUser() {
        val userEmail = uEmail.text.toString()
        val userPassword = uPassword.text.toString()

        if (TextUtils.isEmpty(userEmail)) {
            toast("Enter email address")
            return
        }
        if (TextUtils.isEmpty(userPassword)) {
            toast("Enter password")
            return
        }

        auth.signInWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    if (userPassword.length < 6) {
                        uPassword.error = "Password has to have at least 6 characters"
                    } else {
                        toast("Authentication failed")
                    }
                } else {
                    startActivity(Intent(context, MainActivity::class.java))
                }
            }
    }

    private fun toast(toast_text: String) {
        Toast.makeText(context, toast_text, Toast.LENGTH_SHORT).show()
    }
}