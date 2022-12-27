package com.example.myapplication

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

class TaskContainer private constructor() {
    companion object {
        private val taskContainer: TaskContainer = TaskContainer()

        fun getInstance(): TaskContainer {
            return taskContainer
        }
    }

    private val tasks = ArrayDeque<Task>()
    private var nextId = 0
    var apiRequests = 0
        public get() = field
        private set


    fun getTasks(): List<Task> {
        return tasks
    }

    fun addTask(task: Task) {
        this.tasks.addFirst(Task(task.title, task.desc, task.date))
    }

    fun addTasks(tasks: List<Task>) {
        for (task in tasks)
            addTask(task)
    }

    fun getTask(id: Int): Task {
        return tasks.first { task -> task.id == id }
    }

    fun editTask(task: Task) {
        val was = tasks.removeIf { t -> t.id == task.id }
        if (was)
            addTask(Task(task.title, task.desc, task.date))
    }

    fun addTasksFromApi() {
        val json = try {
            val con =
                URL("http://192.168.2.43:3000/tasks")
                    .openConnection() as HttpURLConnection
            con.connectTimeout = 1000
            con.readTimeout = 1000
            con.content
            con.url.readText()
        } catch (e: java.net.SocketTimeoutException) {
            "[]"
        } catch (e: Exception) {
            throw e
        }

        val tasks: MutableList<Task> = try {
            Json.decodeFromString(json)
        } catch (e: IllegalArgumentException) {
            ArrayDeque()
        } catch (e: Exception) {
            throw e
        }

        apiRequests++
        tasks.sortByDescending { t -> t.date }
        addTasks(tasks)
    }
}