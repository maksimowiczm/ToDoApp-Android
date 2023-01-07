package com.example.myapplication.data.repos

import com.example.myapplication.models.Category

interface ICategoryRepo {
    fun getAll(): List<Category>
    fun addCategory(category: Category)
    fun getCategory(id: Int): Category
    fun updateCategory(category: Category)
    fun deleteCategory(category: Category)
}