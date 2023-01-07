package com.example.myapplication.data.repos

import com.example.myapplication.models.TaskTagCrossRef

interface ITaskTagCrossRefRepo {
    fun getAll(): List<TaskTagCrossRef>
    fun getTaskTagCrossRef(taskId: Int, tagId: Int): TaskTagCrossRef
    fun getTaskTagCrossRefForTask(taskId: Int): List<TaskTagCrossRef>
    fun addTaskTagCrossRef(taskTagCrossRef: TaskTagCrossRef)
    fun deleteTaskTagCrossRef(taskTagCrossRef: TaskTagCrossRef)
}