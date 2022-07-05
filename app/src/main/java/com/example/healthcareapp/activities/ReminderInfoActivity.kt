package com.example.healthcareapp.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.healthcareapp.databinding.ReminderItemInfoBinding
import com.example.healthcareapp.reminder_database.ReminderClass
import java.io.File


class ReminderInfoActivity : AppCompatActivity() {

    private var reminder: ReminderClass? = null
    private lateinit var binding: ReminderItemInfoBinding

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ReminderItemInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        reminder = ReminderClass().apply {
            id  = bundle?.getInt("reminderId")!!
            reminderMedicine = bundle.getString("medicine")
            reminderLabel = bundle.getString("label")
            reminderNotes = bundle.getString("instructions_notes")
            reminderTime = bundle.getString("time")
            reminderDisplayDate = bundle.getString("date")
            reminderDate = bundle.getString("date")
            reminderPhoto = bundle.getString("photo")
            Log.d("Reminder", reminder.toString())
        }

        if (reminder != null) {
            binding.medicineLabelText.setText(reminder!!.reminderMedicine)
            binding.labelText.setText(reminder!!.reminderLabel)
            binding.notesText.setText(reminder!!.reminderNotes)
            binding.timeDescription.text =  reminder!!.reminderTime
            binding.dateDescription.text = reminder!!.reminderDisplayDate
            if (reminder!!.reminderPhoto == null) {
                binding.photoLabel.visibility = View.GONE
                binding.displayPhoto.visibility = View.GONE
            } else {
                var image_path = reminder!!.reminderPhoto
                val file = File(image_path)
                val imageUri = Uri.fromFile(file)
                Glide.with(this)
                    .load(imageUri)
                    .override(1080,1920)
                    .into(binding.displayPhoto);

            }
        }
        reportFullyDrawn()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        val  ReminderActivityIntent = Intent(this, ReminderActivity::class.java)
        startActivity(ReminderActivityIntent)
        finish()
    }

}