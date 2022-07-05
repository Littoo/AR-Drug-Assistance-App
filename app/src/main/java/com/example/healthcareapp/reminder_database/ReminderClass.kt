package com.example.healthcareapp.reminder_database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_table_3")
class ReminderClass {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "reminder_label")
    var reminderLabel: String? = null

    @ColumnInfo(name = "reminder_medicine")
    var reminderMedicine: String? = null

    @ColumnInfo(name = "reminder_date")
    var reminderDate: String? = null

    @ColumnInfo(name = "reminder_display_date")
    var reminderDisplayDate: String? = null

    @ColumnInfo(name = "reminder_time")
    var reminderTime: String? = null

    @ColumnInfo(name = "reminder_notes")
    var reminderNotes: String? = null

    @ColumnInfo(name = "reminder_photo")
    var reminderPhoto: String? = null

    @ColumnInfo(name = "is_done")
    var isDone: Boolean = false
}