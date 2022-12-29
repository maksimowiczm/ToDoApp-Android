package com.example.myapplication

import androidx.lifecycle.LiveData

class TaskLocalRepository(private val taskDao: TaskDao) : ITaskRepository {
    override fun getAll(): LiveData<List<Task>> {
        return taskDao.getAll()
    }

    override fun addTask(task: Task) {
        taskDao.addTask(task)
    }

    override fun getTask(id: Int): Task {
        return taskDao.getTask(id)
    }

    override fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    override fun findTasks(title: String): LiveData<List<Task>> {
        return taskDao.findTask(title)
    }

    override fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
}