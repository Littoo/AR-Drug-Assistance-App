package com.example.healthcareapp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.healthcareapp.activities.ReminderActivity

class BootUpReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val intent1 = Intent(context, ReminderActivity::class.java)
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent1)
    }
}