package com.example.myapplication.repos

import android.content.Context
import androidx.room.*
import com.example.myapplication.converters.InstantConverter
import com.example.myapplication.models.Task

@Database(entities = [Task::class], version = 1, exportSchema = false)
@TypeConverters(InstantConverter::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var taskDatabase: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase {
            if (taskDatabase != null)
                return taskDatabase!!

            synchronized(this) {
                taskDatabase = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                ).build()

                return taskDatabase!!
            }
        }
    }
}