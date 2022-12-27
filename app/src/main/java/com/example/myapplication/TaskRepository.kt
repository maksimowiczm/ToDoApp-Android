package com.example.myapplication

import androidx.lifecycle.LiveData

class TaskRepository(private val taskDao: TaskDao) {
    val getAll: LiveData<List<Task>> = taskDao.getAll()

    fun addTask(task: Task) {
        taskDao.addTask(task)
    }

    fun getTask(id: Int): Task {
        return taskDao.getTask(id)
    }
}