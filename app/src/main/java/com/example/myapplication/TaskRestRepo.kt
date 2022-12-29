package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.kittinunf.fuel.httpGet
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class TaskRestRepo private constructor() : ITaskRepository {
    companion object {
        private val repo: TaskRestRepo = TaskRestRepo()

        fun getInstance(): TaskRestRepo {
            return repo
        }
    }

    private var tasks = ArrayList<Task>()
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
            (ArrayDeque())
        } catch (e: Exception) {
            throw e
        }

        tasks.sortByDescending { t -> t.date }
        tasks.forEach { t -> addTask(t) }
        liveTasks.postValue(tasks)
    }

    override fun getAll(): LiveData<List<Task>> {
        return liveTasks
    }

    override fun addTask(task: Task) {
        tasks.add(task)
    }

    override fun getTask(id: Int): Task {
        return tasks.find { t -> t.id == id }!!
    }

    override fun updateTask(task: Task) {
        TODO("Not yet implemented")
    }

    override fun findTasks(title: String): LiveData<List<Task>> {
        TODO("Not yet implemented")
    }

    override fun deleteTask(task: Task) {
        TODO("Not yet implemented")
    }
}