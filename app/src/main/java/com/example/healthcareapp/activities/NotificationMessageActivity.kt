package com.example.healthcareapp.activities

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.example.healthcareapp.databinding.ActivityNotificationMessageBinding

class NotificationMessageActivity: AppCompatActivity() {

    private lateinit var binding: ActivityNotificationMessageBinding

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        binding = ActivityNotificationMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle: Bundle? = intent.extras

        binding.tvMessage.text = bundle?.getString("message")

    }
}