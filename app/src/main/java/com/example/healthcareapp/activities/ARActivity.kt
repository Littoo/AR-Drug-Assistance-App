package com.example.healthcareapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.example.healthcareapp.R

import com.example.healthcareapp.databinding.ArActivityBinding
import com.example.healthcareapp.fragments.ARFragment


class ARActivity : AppCompatActivity() {

    private lateinit var binding: ArActivityBinding
    private lateinit var fragmentManager: FragmentManager
    private var predicted_drug: String? = null

    private var notes_enabled: Boolean = false
    private var presc_label: String? = null
    private var presc_time: String? = null
    private var presc_notes_instructions: String? = null
    private var presc_photo : String? = null

    private var ARFragment = ARFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ArActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val extras = intent.extras
        if (extras != null) {
            predicted_drug = extras.getString("predictedDrug")

            notes_enabled = extras.getBoolean("notes_enabled", false)
            if (notes_enabled) {
                presc_label = extras.getString("presc_label")
                presc_time = extras.getString("presc_time")
                presc_notes_instructions = extras.getString("presc_notes_instructions")
                presc_photo = extras.getString("presc_photo")
            }

        }
        var bundle = Bundle()
        bundle.putString("predictedDrug", predicted_drug)
        if (notes_enabled) {
            bundle.putBoolean("notes_enabled",notes_enabled)
            bundle.putString("presc_label", presc_label)
            bundle.putString("presc_time", presc_time)
            bundle.putString("presc_notes_instructions", presc_notes_instructions)
            bundle.putString("presc_photo", presc_photo)
        }

        ARFragment = ARFragment()
        ARFragment.arguments = bundle

        fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.containerFragment, ARFragment)
        transaction.setReorderingAllowed(true)
        transaction.commit()
        reportFullyDrawn()
    }

    override fun onBackPressed() {

        var trans = fragmentManager.beginTransaction();
        trans.hide(ARFragment)
        trans.addToBackStack(null);
        trans.commit();

        val  HomeActivityIntent = Intent(this, HomeActivity::class.java)
        HomeActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(HomeActivityIntent)
        finish()

        super.onBackPressed()
    }
}