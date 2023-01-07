package com.example.myapplication.data.daos

import androidx.room.*
import com.example.myapplication.models.TaskTagCrossRef

@Dao
interface TaskTagCrossRefDao {
    @Query("SELECT * FROM tasktags")
    fun getAll(): List<TaskTagCrossRef>

    @Query("SELECT * FROM tasktags WHERE tagId=:tagId AND taskId=:taskId LIMIT 1")
    fun getTaskTagCrossRef(taskId: Int, tagId: Int): TaskTagCrossRef

    @Query("SELECT * FROM tasktags WHERE taskId=:taskId")
    fun getTaskTagCrossRefForTask(taskId: Int): List<TaskTagCrossRef>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addTaskTagCrossRef(taskTagCrossRef: TaskTagCrossRef)

    @Delete
    fun deleteTaskTagCrossRef(taskTagCrossRef: TaskTagCrossRef)
}