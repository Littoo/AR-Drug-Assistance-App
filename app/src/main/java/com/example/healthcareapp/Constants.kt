package com.example.healthcareapp

import android.Manifest

object Constants {

    const val TAG = "cameraX"
    const val FILE_NAME_FORMAT = "yy-MM-dd-HH-mm-ss-SSS"
    const val REQUEST_CODE_PERMISSIONS = 123
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    var startTime: Long = 0
    var iscRunning= false

    var user_name: String? = null

    val SHORT_MONTHS =
        arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val LONG_MONTHS =
        arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    val DRUGS_LIST = arrayOf(
        "Forxiga",
        "Jardiance Duo",
        "Diamicron",
        "Getryl",
        "Trajenta Duo",
        "Formet 500",
        "Zemiglo",
        "Piozone",
        "Xelevia",
        "Galvusmet"
    )
//
//    companion object{
//
//        var iscRunning= false
//    }
}