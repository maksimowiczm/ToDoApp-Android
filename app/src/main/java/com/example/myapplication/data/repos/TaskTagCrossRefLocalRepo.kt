package com.example.myapplication.data.repos

import com.example.myapplication.data.daos.TaskTagCrossRefDao
import com.example.myapplication.models.TaskTagCrossRef

class TaskTagCrossRefLocalRepo(private val taskTagCrossRefDao: TaskTagCrossRefDao) :
    ITaskTagCrossRefRepo {
    override fun getAll(): List<TaskTagCrossRef> {
        return taskTagCrossRefDao.getAll()
    }

    override fun getTaskTagCrossRef(taskId: Int, tagId: Int): TaskTagCrossRef {
        return taskTagCrossRefDao.getTaskTagCrossRef(taskId, tagId)
    }

    override fun getTaskTagCrossRefForTask(taskId: Int): List<TaskTagCrossRef> {
        return taskTagCrossRefDao.getTaskTagCrossRefForTask(taskId)
    }

    override fun addTaskTagCrossRef(taskTagCrossRef: TaskTagCrossRef) {
        return taskTagCrossRefDao.addTaskTagCrossRef(taskTagCrossRef)
    }

    override fun deleteTaskTagCrossRef(taskTagCrossRef: TaskTagCrossRef) {
        return taskTagCrossRefDao.deleteTaskTagCrossRef(taskTagCrossRef)
    }
}