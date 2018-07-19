package co.sancarbar.hampi.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import co.sancarbar.hampi.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

/**
 * @author Santiago Carrillo
 * 7/19/18.
 */
class LoginActivity : Activity(), FirebaseAuth.AuthStateListener, View.OnClickListener {

    private lateinit var googleSignInClient: GoogleSignInClient

    private var firebaseAuth = FirebaseAuth.getInstance()

    private val RC_SIGN_IN = 9001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        signInButton.setSize(SignInButton.SIZE_WIDE)
        signInButton.setOnClickListener(this)
        initGoogleSignInClient()
    }



    private fun initGoogleSignInClient() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }


    override fun onClick(v: View?) {
        signInButton.isEnabled = false
        startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }


    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {

    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(this)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this
        ) { task ->
            if (task.isSuccessful) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                enableSignInButton()
            }
            signInButton.isEnabled = true
        }
    }


    private fun enableSignInButton() {
        runOnUiThread { signInButton.isEnabled = false }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                enableSignInButton()
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}