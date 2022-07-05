package com.example.healthcareapp.reminder_database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface ReminderDao {
    @Insert
    fun insertAll(reminder: ReminderClass?): Long

    @Query("SELECT * FROM reminder_table_3")
    fun getReminders(): List<ReminderClass>

    @Query("DELETE FROM reminder_table_3 WHERE id = :reminderId ")
    fun deleteReminder(reminderId: Int)
}