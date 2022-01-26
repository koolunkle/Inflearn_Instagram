package com.inflearn.instagramcopy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        email_login_button.setOnClickListener {
            signInAndSignUp()
        }
    }

    fun signInAndSignUp() {
        auth?.createUserWithEmailAndPassword(
            email_editText.text.toString(),
            password_editText.text.toString()
        )?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
//                Creating a user account
                moveMainInPage(task.result?.user)
            } else if (task.exception?.message.isNullOrEmpty()) {
//                Show the error message
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
            } else {
//                Login if you have account
                signInEmail()
            }
        }
    }

    fun signInEmail() {
        auth?.signInWithEmailAndPassword(
            email_editText.text.toString(),
            password_editText.text.toString()
        )?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
//                    Login
                moveMainInPage(task.result?.user)
            } else {
//                    Show the error message
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun moveMainInPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

}