package com.example.myapplication.data.repos

import androidx.lifecycle.LiveData
import com.example.myapplication.models.TaskTagCrossRef

interface ITaskTagCrossRefRepo {
    fun getAll(): LiveData<List<TaskTagCrossRef>>
    fun getTaskTagCrossRef(taskId: Int, tagId: Int): TaskTagCrossRef
    fun getTaskTagCrossRefForTask(taskId: Int): LiveData<List<TaskTagCrossRef>>
    fun addTaskTagCrossRef(taskTagCrossRef: TaskTagCrossRef)
    fun deleteTaskTagCrossRef(taskTagCrossRef: TaskTagCrossRef)
}