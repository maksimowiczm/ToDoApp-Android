package com.example.myapplication.data.repos

import com.example.myapplication.Settings
import com.example.myapplication.models.Category
import com.github.kittinunf.fuel.*
import com.github.kittinunf.fuel.core.extensions.jsonBody
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CategoryRestRepo private constructor() : ICategoryRepo {
    private val server = Settings.REST_ADDRESS

    companion object {
        private val repo: CategoryRestRepo = CategoryRestRepo()

        fun getInstance(): CategoryRestRepo {
            return repo
        }
    }

    override fun getAll(): List<Category> {
        return getCategoriesApi()
    }

    override fun addCategory(category: Category) {
        addCategoryApi(category)
    }

    override fun getCategory(id: Int): Category {
        return getCategoryApi(id)
    }

    override fun updateCategory(category: Category) {
        updateCategoryApi(category)
    }

    override fun deleteCategory(category: Category) {
        deleteCategoryApi(category.id)
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

    private fun getCategoriesApi(): List<Category> {
        var categories = emptyList<Category>()
        Fuel.get("${server}categories")
            .timeout(3000)
            .response { _, response, result ->
                val (_, error) = result

                if (error != null)
                    throw Exception("lol")

                val categoriesJson = String(response.data)

               categories = try {
                    Json.decodeFromString(categoriesJson)
                } catch (e: Exception) {
                    throw e
                }
            }
        return categories
    }

    private fun getCategoryApi(id: Int): Category {
        val (_, response, result) =
            ("${server}categories/${id}")
                .httpGet()
                .timeout(3000)
                .response()

        val (_, error) = result

        if (error != null)
            throw Exception("lol")

        val categoriesJson = String(response.data)

        val category: Category = try {
            Json.decodeFromString(categoriesJson)
        } catch (e: Exception) {
            throw e
        }

        return category
    }

    private fun addCategoryApi(category: Category) {
        val (_, _, result) =
            ("${server}categories")
                .httpPost()
                .jsonBody(Json.encodeToString(category))
                .timeout(3000)
                .response()

        val (_, error) = result

        if (error != null)
            throw Exception("lol")
    }

    private fun updateCategoryApi(category: Category) {
        val id = category.id
        val (_, _, result) =
            ("${server}categories/${id}")
                .httpPatch()
                .jsonBody(Json.encodeToString(category))
                .timeout(3000)
                .response()

        val (_, error) = result

        if (error != null)
            throw Exception("lol")
    }

    private fun deleteCategoryApi(id: Int) {
        val (_, _, result) =
            ("${server}categories/${id}")
                .httpDelete()
                .timeout(3000)
                .response()

        val (_, error) = result

        if (error != null)
            throw Exception("lol")
    }
}