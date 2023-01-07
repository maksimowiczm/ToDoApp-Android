package com.example.myapplication.data

import android.content.Context
import androidx.room.*
import com.example.myapplication.converters.InstantConverter
import com.example.myapplication.data.daos.CategoryDao
import com.example.myapplication.data.daos.TagDao
import com.example.myapplication.data.daos.TaskDao
import com.example.myapplication.data.daos.TaskTagCrossRefDao
import com.example.myapplication.models.Category
import com.example.myapplication.models.Tag
import com.example.myapplication.models.Task
import com.example.myapplication.models.TaskTagCrossRef

//TODO: eventually change database name (will remove data)
@Database(
    entities = [Task::class, Category::class, Tag::class, TaskTagCrossRef::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration (from = 1, to = 2)
    ])
@TypeConverters(InstantConverter::class)
abstract class ToDoDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun tagDao(): TagDao
    abstract fun categoryDao(): CategoryDao
    abstract fun taskTagCrossRefDao(): TaskTagCrossRefDao

    companion object {
        @Volatile
        private var toDoDatabase: ToDoDatabase? = null

        fun getInstance(context: Context): ToDoDatabase {
            if (toDoDatabase != null)
                return toDoDatabase!!

            synchronized(this) {
                toDoDatabase = Room.databaseBuilder(
                    context.applicationContext,
                    ToDoDatabase::class.java,
                    "task_database"
                ).build()

                return toDoDatabase!!
            }
        }
    }
}