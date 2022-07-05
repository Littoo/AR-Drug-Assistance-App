package com.example.healthcareapp.utils

import android.util.Log
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class TimeUtil () {

    fun FormatTime(hour: Int, minute: Int): String? {
        var time: String
        time = ""
        val formattedMinute: String
        formattedMinute = if (minute / 10 == 0) {
            "0$minute"
        } else {
            "" + minute
        }
        time = if (hour == 0) {
            "12:$formattedMinute AM"
        } else if (hour < 12) {
            "$hour:$formattedMinute AM"
        } else if (hour == 12) {
            "12:$formattedMinute PM"
        } else {
            val temp = hour - 12
            "$temp:$formattedMinute PM"
        }
        return time
    }

    fun formatDate(date: String?): String{

        val input = SimpleDateFormat("d-m-yyyy", Locale.US)
        val output = SimpleDateFormat("d MMMM yyyy", Locale.US)

        val inputFormatted: Date = input.parse(date)!!
        val outputFormatted = output.format(date)

        Log.d("Formatted Date", "The new date is $outputFormatted $inputFormatted")
        return  outputFormatted
    }
}

