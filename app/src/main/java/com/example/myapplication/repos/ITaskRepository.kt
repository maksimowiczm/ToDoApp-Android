package com.example.myapplication.repos

import androidx.lifecycle.LiveData
import com.example.myapplication.models.Task

interface ITaskRepository {
    fun getAll(): LiveData<List<Task>>
    fun addTask(task: Task)
    fun getTask(id: Int): Task
    fun updateTask(task: Task)
    fun findTasks(query: String): LiveData<List<Task>>
    fun deleteTask(task: Task)
}