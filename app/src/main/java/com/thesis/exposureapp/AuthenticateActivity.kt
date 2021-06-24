package com.thesis.exposureapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction


class AuthenticateActivity : AppCompatActivity(R.layout.authenticate_activity) {
    private val loginFragment: LoginFragment = LoginFragment()
    private val registerFragment: RegisterFragment = RegisterFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        switchFragments(savedInstanceState, loginFragment)

        val buttonLogin: Button = findViewById(R.id.button_login)
        buttonLogin.setOnClickListener {
            switchFragments(savedInstanceState, loginFragment)
        }

        val buttonRegister: Button = findViewById(R.id.button_register)
        buttonRegister.setOnClickListener {
            switchFragments(savedInstanceState, registerFragment)
        }

    }

    private fun switchFragments(savedInstanceState: Bundle?, fragment: Fragment) {
        if (savedInstanceState == null) {
            supportFragmentManager.inTransaction {
                replace(R.id.fragment_container_view, fragment)
            }
        }
    }

}

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}
