package com.example.healthcareapp.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.Constants
import com.example.healthcareapp.adapter.ReminderAdapter
import com.example.healthcareapp.databinding.ActivityReminderBinding
import com.example.healthcareapp.reminder_database.DatabaseClass
import com.example.healthcareapp.reminder_database.ReminderClass
import com.example.healthcareapp.utils.WindowUIController

class ReminderActivity: AppCompatActivity(){

    private lateinit var recylerView : RecyclerView
    private lateinit var binding: ActivityReminderBinding
    private var databaseClass: DatabaseClass? = null
    private lateinit var reminderAdapter: ReminderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("Time elapsed: ", "Time elapsed for opening Reminder: ${SystemClock.elapsedRealtime() - Constants.startTime} millis")
        supportActionBar?.title = "Reminder"

        recylerView = binding.remindersRecyclerView
        recylerView.layoutManager = LinearLayoutManager(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowUIController().hideNavigationBar(window)
        }

        databaseClass = DatabaseClass.getDatabase(applicationContext)

        binding.floatingActionButton.show()
        binding.floatingActionButton.setOnClickListener {
            Constants.startTime = SystemClock.elapsedRealtime()
            val CreateReminderIntent = Intent (this, CreateReminderActivity::class.java)
            startActivity(CreateReminderIntent)
            finish()
        }
        reportFullyDrawn()
    }

    override fun onStart() {
        super.onStart()
        val extras = intent.extras
        if (extras != null) {
            val id = extras.getInt("reminderId")
            onDeleteReminder(id)
        }
    }

    override fun onResume() {
        super.onResume()
        setRemindersAdapter()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val  HomeActivityIntent = Intent(this, HomeActivity::class.java)
        startActivity(HomeActivityIntent)
        finish()
    }

    private fun setRemindersAdapter(){

        val reminderList: List<ReminderClass>? = databaseClass?.ReminderDao()?.getReminders()
        if (!reminderList.isNullOrEmpty()) {
            val reminderAdapter: ReminderAdapter = ReminderAdapter(reminderList, applicationContext )
            recylerView.adapter = reminderAdapter
        }
    }

    private fun onDeleteReminder(id: Int) {
        databaseClass?.ReminderDao()?.deleteReminder(id)
    }

}
