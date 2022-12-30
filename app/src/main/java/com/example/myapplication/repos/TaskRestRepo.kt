package com.example.myapplication.repos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.models.Task
import com.github.kittinunf.fuel.*
import com.github.kittinunf.fuel.core.extensions.jsonBody
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


private const val SERVER = "http://192.168.2.43:3000/"

class TaskRestRepo private constructor() : ITaskRepository {
    companion object {
        private val repo: TaskRestRepo = TaskRestRepo()

        fun getInstance(): TaskRestRepo {
            return repo
        }
    }

    private fun notifyObserver() {
        getTasksApi()
    }

    override fun getAll(): LiveData<List<Task>> {
        return getTasksApi()
    }

    override fun addTask(task: Task) {
        addTaskApi(task)
    }

    override fun getTask(id: Int): Task {
        return getTaskApi(id)
    }

    override fun updateTask(task: Task) {
        updateTaskApi(task)
    }

    override fun findTasks(query: String): LiveData<List<Task>> {
        return findTasksApi(query)
    }

    override fun deleteTask(task: Task) {
        deleteTaskApi(task.id)
    }


    private val liveTasks = MutableLiveData<List<Task>>()

    fun getStatus(): Boolean {
        val (_, _, result) =
            (SERVER)
                .httpGet()
                .timeout(1000)
                .response()

        val (_, error) = result

        if (error != null)
            return false

        return true
    }

    private fun getTasksApi(): LiveData<List<Task>> {
        Fuel.get("${SERVER}tasks")
            .timeout(3000)
            .response { _, response, result ->
                val (_, error) = result

                if (error != null)
                    throw Exception("lol")

                val tasksJson = String(response.data)

                val tasks: ArrayList<Task> = try {
                    Json.decodeFromString(tasksJson)
                } catch (e: Exception) {
                    throw e
                }

                tasks.sortByDescending { it.date }
                liveTasks.postValue(tasks)
            }

        return liveTasks
    }

    private fun getTaskApi(id: Int): Task {
        val (_, response, result) =
            ("${SERVER}tasks/${id}")
                .httpGet()
                .timeout(3000)
                .response()

        val (_, error) = result

        if (error != null)
            throw Exception("lol")

        val tasksJson = String(response.data)

        val task: Task = try {
            Json.decodeFromString(tasksJson)
        } catch (e: Exception) {
            throw e
        }

        return task
    }

    private fun addTaskApi(task: Task) {
        val (_, _, result) =
            ("${SERVER}tasks")
                .httpPost()
                .jsonBody(Json.encodeToString(task))
                .timeout(3000)
                .response()

        val (_, error) = result

        if (error != null)
            throw Exception("lol")

        notifyObserver()
    }

    private fun updateTaskApi(task: Task) {
        val id = task.id
        val (_, _, result) =
            ("${SERVER}tasks/${id}")
                .httpPatch()
                .jsonBody(Json.encodeToString(task))
                .timeout(3000)
                .response()

        val (_, error) = result

        if (error != null)
            throw Exception("lol")

        notifyObserver()
    }

    private fun deleteTaskApi(id: Int) {
        val (_, _, result) =
            ("${SERVER}tasks/${id}")
                .httpDelete()
                .timeout(3000)
                .response()

        val (_, error) = result

        if (error != null)
            throw Exception("lol")

        notifyObserver()
    }

    private val queryList = MutableLiveData<List<Task>>()
    private fun findTasksApi(query: String): LiveData<List<Task>> {
        val tasks = liveTasks.value!!

        val queryTasks = ArrayList<Task>()
        for (task in tasks.iterator()) {
            if (task.desc.contains(query) || (task.title != null && task.title!!.contains(query)))
                queryTasks.add(task)
        }

        queryList.postValue(queryTasks)
        return queryList
    }
}