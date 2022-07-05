package com.example.healthcareapp.utils

import android.os.Build
import android.view.Window
import androidx.annotation.RequiresApi
import com.example.healthcareapp.activities.ReminderActivity

class WindowUIController () {

    @RequiresApi(Build.VERSION_CODES.R)
    fun hideNavigationBar(window: Window) {
        window.decorView.windowInsetsController!!.hide(
            android.view.WindowInsets.Type.statusBars()
                    or android.view.WindowInsets.Type.navigationBars()
        )
    }
}