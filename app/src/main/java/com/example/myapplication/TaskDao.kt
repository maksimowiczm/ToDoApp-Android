package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAll(): LiveData<List<Task>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addTask(task: Task)
}