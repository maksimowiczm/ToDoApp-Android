package com.example.myapplication.data.repos

import androidx.lifecycle.LiveData
import com.example.myapplication.models.Task

interface ITaskRepo {
    fun getAll(): LiveData<List<Task>>
    fun addTask(task: Task)
    fun getTask(id: Int): Task
    fun updateTask(task: Task)
    fun findTasks(query: String): LiveData<List<Task>>
    fun deleteTask(task: Task)
}