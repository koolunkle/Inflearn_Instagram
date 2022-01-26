package com.inflearn.instagramcopy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    var auth: FirebaseAuth? = null

    var googleSignInClient: GoogleSignInClient? = null

    var GOOGLE_LOGIN_CODE = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        email_login_button.setOnClickListener {
            signInAndSignUp()
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("684435037531-2i97j2sqdfm179o2aaag7gp63nkdnkfv.apps.googleusercontent.com")
            .requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        google_sign_in_button.setOnClickListener {
//            First step
            googleLogin()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE) {
            var result = data?.let { Auth.GoogleSignInApi.getSignInResultFromIntent(it) }
            if (result != null) {
                if (result.isSuccess) {
                    var account = result.signInAccount
//                    Second step
                    firebaseAuthWithGoogle(account)
                }
            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
//                    Login
                moveMainInPage(task.result?.user)
            } else {
//                    Show the error message
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
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