package com.example.healthcareapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.activities.LoginActivity
import com.example.healthcareapp.adapter.DrugInformationAdapter
import com.example.healthcareapp.models.drugs.OpenFDAResponse
import com.example.healthcareapp.viewmodel.DrugInformationViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.healthcareapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding

    lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var auth: FirebaseAuth
    lateinit var drugInfoInitViewModel: DrugInformationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        maybeEnableArButton()

        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)

        binding.getButton.setOnClickListener { loadData() }

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

//    fun maybeEnableArButton() {
//        val availability = ArCoreApk.getInstance().checkAvailability(this)
//        if (availability.isTransient) {
//            // Continue to query availability at 5Hz while compatibility is checked in the background.
//            Handler().postDelayed({
//                maybeEnableArButton()
//            }, 200)
//        }
//        if (availability.isSupported) {
//            val mArButton
//            mArButton.visibility = View.VISIBLE
//            mArButton.isEnabled = true
//        } else { // The device is unsupported or unknown.
//            mArButton.visibility = View.INVISIBLE
//            mArButton.isEnabled = false
//        }
//    }

    fun loadData(){
        drugInfoInitViewModel = ViewModelProvider(this).get(DrugInformationViewModel::class.java)
        drugInfoInitViewModel.getApiData()
        drugInfoInitViewModel.OpenFDAData.observe(this, Observer {
            initAdapter(it)
        })
    }

    fun initAdapter(data: OpenFDAResponse){
        val rvDrugInfo = findViewById<RecyclerView>(R.id.recyclerVi)
        rvDrugInfo.layoutManager = LinearLayoutManager(this)
        // This will pass the ArrayList to our Adapter
        val adapter = DrugInformationAdapter(data)
        // Setting the Adapter with the recyclerview
        rvDrugInfo.adapter = adapter
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
        // Navigate to LoginActivity
    }
}