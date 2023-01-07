package com.example.myapplication.data.repos

import com.example.myapplication.Settings
import com.example.myapplication.models.Tag
import com.github.kittinunf.fuel.*
import com.github.kittinunf.fuel.core.extensions.jsonBody
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TagRestRepo private constructor() : ITagRepo {
    private val server = Settings.REST_ADDRESS

    companion object {
        private val repo: TagRestRepo = TagRestRepo()

        fun getInstance(): TagRestRepo {
            return repo
        }
    }

    override fun getAll(): List<Tag> {
        return getTagsApi()
    }

    override fun addTag(tag: Tag) {
        addTagApi(tag)
    }

    override fun getTag(id: Int): Tag {
        return getTagApi(id)
    }

    override fun updateTag(tag: Tag) {
        updateTagApi(tag)
    }

    override fun deleteTag(tag: Tag) {
        deleteTagApi(tag.id)
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

    private fun getTagsApi(): List<Tag> {
        var tags = emptyList<Tag>()
        Fuel.get("${server}tags")
            .timeout(3000)
            .response { _, response, result ->
                val (_, error) = result

                if (error != null)
                    throw Exception("lol")

                val tagsJson = String(response.data)

               tags = try {
                    Json.decodeFromString(tagsJson)
                } catch (e: Exception) {
                    throw e
                }
            }
        return tags
    }

    private fun getTagApi(id: Int): Tag {
        val (_, response, result) =
            ("${server}tags/${id}")
                .httpGet()
                .timeout(3000)
                .response()

        val (_, error) = result

        if (error != null)
            throw Exception("lol")

        val tagsJson = String(response.data)

        val tag: Tag = try {
            Json.decodeFromString(tagsJson)
        } catch (e: Exception) {
            throw e
        }

        return tag
    }

    private fun addTagApi(tag: Tag) {
        val (_, _, result) =
            ("${server}tags")
                .httpPost()
                .jsonBody(Json.encodeToString(tag))
                .timeout(3000)
                .response()

        val (_, error) = result

        if (error != null)
            throw Exception("lol")
    }

    private fun updateTagApi(tag: Tag) {
        val id = tag.id
        val (_, _, result) =
            ("${server}tags/${id}")
                .httpPatch()
                .jsonBody(Json.encodeToString(tag))
                .timeout(3000)
                .response()

        val (_, error) = result

        if (error != null)
            throw Exception("lol")
    }

    private fun deleteTagApi(id: Int) {
        val (_, _, result) =
            ("${server}tags/${id}")
                .httpDelete()
                .timeout(3000)
                .response()

        val (_, error) = result

        if (error != null)
            throw Exception("lol")
    }
}