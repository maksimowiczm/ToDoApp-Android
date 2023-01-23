package com.example.myapplication.data.repos

import androidx.lifecycle.LiveData
import com.example.myapplication.data.daos.TaskDao
import com.example.myapplication.models.Task

class TaskLocalRepo(private val taskDao: TaskDao) : ITaskRepo {
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

    override fun findTasks(query: String): LiveData<List<Task>> {
        return taskDao.findTask(query)
    }

    override fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
}