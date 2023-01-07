package com.example.myapplication.data.repos

import com.example.myapplication.Settings
import com.example.myapplication.models.TaskTagCrossRef
import com.example.myapplication.models.TaskTagJsonAdapter
import com.github.kittinunf.fuel.*
import com.github.kittinunf.fuel.core.extensions.jsonBody
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TaskTagCrossRefRestRepo private constructor() : ITaskTagCrossRefRepo {
    private val server = Settings.REST_ADDRESS

    companion object {
        private val repo: TaskTagCrossRefRestRepo = TaskTagCrossRefRestRepo()

        fun getInstance(): TaskTagCrossRefRestRepo {
            return repo
        }
    }

    override fun getAll(): List<TaskTagCrossRef> {
        return getTaskTagsCrossRefApi()
    }

    override fun addTaskTagCrossRef(taskTagCrossRef: TaskTagCrossRef) {
        addTaskTagCrossRefApi(taskTagCrossRef)
    }

    override fun getTaskTagCrossRef(taskId: Int, tagId: Int): TaskTagCrossRef {
        return getTaskTagCrossRefApi(taskId, tagId)
    }

    override fun deleteTaskTagCrossRef(taskTagCrossRef: TaskTagCrossRef) {
        deleteTagApi(taskTagCrossRef)
    }

    override fun getTaskTagCrossRefForTask(taskId: Int): List<TaskTagCrossRef> {
        return getTaskTagCrossRefForTaskApi(taskId)
    }

    fun getStatus(): Boolean {
        val (_, _, result) =
            (server)
                .httpGet()
                .timeout(2000)
                .response()

        val (_, error) = result

        if (error != null)
            return false

        return true
    }

    private fun getTaskTagsCrossRefApi(): List<TaskTagCrossRef> {
        var taskTags = emptyList<TaskTagJsonAdapter>()
        Fuel.get("${server}tasktags")
            .timeout(3000)
            .response { _, response, result ->
                val (_, error) = result

                if (error != null)
                    throw Exception("lol")

                val taskTagsJson = String(response.data)

               taskTags = try {
                    Json.decodeFromString(taskTagsJson)
                } catch (e: Exception) {
                    throw e
                }
            }
        val newTaskTags = ArrayList<TaskTagCrossRef>()
        taskTags.forEach { newTaskTags.add(TaskTagCrossRef(it.taskId,it.tagId)) }
        return newTaskTags.toList()
    }

    private fun getTaskTagCrossRefApiJson(taskId: Int, tagId: Int): TaskTagJsonAdapter {
        val taskTags: List<TaskTagJsonAdapter>
        val (_, response, result) =
            ("${server}tasktags?taskId=${taskId}&tagId=${tagId}")
                .httpGet()
                .timeout(3000)
                .response()

        val (_, error) = result

        if (error != null)
            throw Exception("lol")

        val taskTagsJson = String(response.data)

        taskTags = try {
            Json.decodeFromString(taskTagsJson)
        } catch (e: Exception) {
            throw e
        }

        return taskTags.first { it.taskId == taskId && it.tagId == tagId }
    }

    private fun getTaskTagCrossRefApi(taskId: Int, tagId: Int): TaskTagCrossRef {
        val taskTags: List<TaskTagJsonAdapter>
        val (_, response, result) =
            ("${server}tasktags?taskId=${taskId}&tagId=${tagId}")
                .httpGet()
                .timeout(3000)
                .response()

        val (_, error) = result

        if (error != null)
            throw Exception("lol")

        val taskTagsJson = String(response.data)

        taskTags = try {
            Json.decodeFromString(taskTagsJson)
        } catch (e: Exception) {
            throw e
        }

        val tmp = taskTags.first { it.taskId == taskId && it.tagId == tagId }
        return TaskTagCrossRef(tmp.taskId, tmp.tagId)
    }

    private fun addTaskTagCrossRefApi(taskTagCrossRef: TaskTagCrossRef) {
        val taskTagJsonAdapter = TaskTagJsonAdapter(taskTagCrossRef.taskId, taskTagCrossRef.tagId)
        val (_, _, result) =
            ("${server}tasktags")
                .httpPost()
                .jsonBody(Json.encodeToString(taskTagJsonAdapter))
                .timeout(3000)
                .response()

        val (_, error) = result

        if (error != null)
            throw Exception("lol")
    }

    private fun deleteTagApi(taskTagCrossRef: TaskTagCrossRef) {
        val taskTagJson = getTaskTagCrossRefApiJson(taskTagCrossRef.taskId, taskTagCrossRef.tagId)

        val (_, _, result) =
            ("${server}tags/${taskTagJson.id}")
                .httpDelete()
                .timeout(3000)
                .response()

        val (_, error) = result

        if (error != null)
            throw Exception("lol")
    }

    private fun getTaskTagCrossRefForTaskApi(taskId: Int): List<TaskTagCrossRef>{
        var taskTags = emptyList<TaskTagJsonAdapter>()
        Fuel.get("${server}tasktags?taskId=${taskId}")
            .timeout(3000)
            .response { _, response, result ->
                val (_, error) = result

                if (error != null)
                    throw Exception("lol")

                val taskTagsJson = String(response.data)

                taskTags = try {
                    Json.decodeFromString(taskTagsJson)
                } catch (e: Exception) {
                    throw e
                }
            }
        val newTaskTags = ArrayList<TaskTagCrossRef>()
        taskTags.forEach { newTaskTags.add(TaskTagCrossRef(it.taskId,it.tagId)) }
        return newTaskTags.toList()
    }
}