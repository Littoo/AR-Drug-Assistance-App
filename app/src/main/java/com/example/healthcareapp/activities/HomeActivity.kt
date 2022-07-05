package com.example.healthcareapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.healthcareapp.Constants
import com.example.healthcareapp.R
import com.example.healthcareapp.activities.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.healthcareapp.databinding.ActivityHomeBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInResult

class HomeActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "HomeActivity"
    }

    private lateinit var binding: ActivityHomeBinding

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("Time elapsed: ", "Time elapsed : ${SystemClock.elapsedRealtime() - Constants.startTime} millis")

//        maybeEnableArButton()
        auth = Firebase.auth
        supportActionBar?.title = ""

        val extras = intent.extras
        if (extras != null) {
            Constants.user_name= extras.getString("userName").toString()
        }

        binding.userName.text = Constants.user_name.toString()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)

        binding.homeButton1.setOnClickListener {
            Constants.startTime = SystemClock.elapsedRealtime()
            val Cameraintent = Intent (this, CameraActivity::class.java)
            Cameraintent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(Cameraintent)
            finish()
        }

        binding.homeButton2.setOnClickListener {
            Constants.startTime = SystemClock.elapsedRealtime()
            val ReminderIntent = Intent (this, ReminderActivity::class.java)
            ReminderIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(ReminderIntent)
            finish()
        }
        reportFullyDrawn()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu )
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
         if (item.itemId == R.id.miLogout) {
             Log.i(TAG, "Logout")
             auth.signOut()
             var currentUser = auth.currentUser
             val  logoutIntent = Intent(this, LoginActivity::class.java)
             logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
             updateUI(currentUser,logoutIntent )

         }
        return super.onOptionsItemSelected(item)
    }

    private fun updateUI(user: FirebaseUser?, logoutIntent: Intent) {
        if (user != null) {
            Log.i(TAG, "user logging out failed..")
            return
        }
        mGoogleSignInClient.signOut()
        Log.i(TAG, "user logged out successfully..")
        startActivity(logoutIntent)
        finish()
    }
}