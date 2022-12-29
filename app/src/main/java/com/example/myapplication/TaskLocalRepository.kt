package com.example.myapplication

import androidx.lifecycle.LiveData

class TaskLocalRepository(private val taskDao: TaskDao) {
    val getAll: LiveData<List<Task>> = taskDao.getAll()

    fun addTask(task: Task) {
        taskDao.addTask(task)
    }

    fun getTask(id: Int): Task {
        return taskDao.getTask(id)
    }

    fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    fun findTasks(title: String): LiveData<List<Task>> {
        return taskDao.findTask(title)
    }

    fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
}