package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class TaskRestRepo private constructor() : ITaskRepository {
    companion object {
        private val repo: TaskRestRepo = TaskRestRepo()

        fun getInstance(): TaskRestRepo {
            return repo
        }
    }

    private var tasks = ArrayDeque<Task>()
    private var liveTasks = MutableLiveData<List<Task>>(tasks)

    fun loadFromApi() {
        val (_, response, result) =
            "http://192.168.2.43:3000/tasks"
                .httpGet()
                .timeout(3000)
                .response()

        val (_, error) = result

        if (error != null)
            throw Exception("lol")

        val tasksJson = String(response.data)

        val tasks: MutableList<Task> = try {
            Json.decodeFromString(tasksJson)
        } catch (e: IllegalArgumentException) {
            ArrayDeque()
        } catch (e: Exception) {
            throw e
        }

        tasks.sortByDescending { t -> t.date }
        tasks.forEach { t -> localAddTask(t) }
        liveTasks.postValue(tasks)
    }

    override fun getAll(): LiveData<List<Task>> {
        return liveTasks
    }

    private fun localAddTask(task: Task) {
        tasks.addFirst(task)
    }

    override fun addTask(task: Task) {
        val (_, response, result) =
            "http://192.168.2.43:3000/tasks"
                .httpPost()
                .jsonBody(Json.encodeToString(task))
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

        localAddTask(task)
        liveTasks.postValue(tasks)
    }

    override fun getTask(id: Int): Task {
        return tasks.find { t -> t.id == id }!!
    }

    override fun updateTask(task: Task) {
        TODO("Not yet implemented")
    }

    override fun findTasks(query: String): LiveData<List<Task>> {
        TODO("Not yet implemented")
    }

    override fun deleteTask(task: Task) {
        TODO("Not yet implemented")
    }
}