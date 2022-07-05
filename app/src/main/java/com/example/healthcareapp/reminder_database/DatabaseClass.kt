package com.example.healthcareapp.reminder_database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(
    entities = [ReminderClass::class],
    version = 1,
 )
abstract class DatabaseClass : RoomDatabase() {
    abstract fun ReminderDao(): ReminderDao

    companion object {
        private var INSTANCE: DatabaseClass? = null
        fun getDatabase(context: Context): DatabaseClass? {
            if (INSTANCE == null) {
                synchronized(DatabaseClass::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            DatabaseClass::class.java,
                            "healthcare_database_3"
                        ).allowMainThreadQueries()
//                            .addMigrations(MIGRATION_1_2)
//                            .fallbackToDestructiveMigration()
                            .build()
                    }
                }
            }
            return INSTANCE
        }

//        val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE ReminderTable ADD COLUMN reminder_display_date STRING ")
//            }
//        }


    }
}

