package com.example.myapplication

import android.content.Context
import androidx.room.*
import com.example.myapplication.converters.Converters

@Database(entities = [Task::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
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