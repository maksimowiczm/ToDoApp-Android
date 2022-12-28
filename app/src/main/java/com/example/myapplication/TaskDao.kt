package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY date DESC")
    fun getAll(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE id=:id LIMIT 1")
    fun getTask(id: Int): Task

    @Update
    fun updateTask(task: Task)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addTask(task: Task)

    @Query("select * from tasks where title like '%' || :title || '%' ORDER BY date DESC")
    fun findTask(title: String): LiveData<List<Task>>

    @Delete
    fun deleteTask(task: Task)
}