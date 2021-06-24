package com.thesis.exposureapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction


class AuthenticateActivity : AppCompatActivity(R.layout.authenticate_activity) {
    private val loginFragment: LoginFragment = LoginFragment()
    private val registerFragment: RegisterFragment = RegisterFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.inTransaction {
                add(R.id.fragment_container_view, loginFragment)
            }
        }
        val buttonLogin: Button = findViewById(R.id.button_login)
        val buttonRegister: Button = findViewById(R.id.button_register)

        buttonLogin.setOnClickListener {
            if (savedInstanceState == null) {
                supportFragmentManager.inTransaction {
                    replace(R.id.fragment_container_view, loginFragment)
                }
            }
        }

        buttonRegister.setOnClickListener {
            if (savedInstanceState == null) {
                supportFragmentManager.inTransaction {
                    replace(R.id.fragment_container_view, registerFragment)
                }
            }
        }

    }
}

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}
