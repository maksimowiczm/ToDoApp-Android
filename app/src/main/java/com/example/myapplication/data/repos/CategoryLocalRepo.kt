package com.example.myapplication.data.repos

import androidx.lifecycle.LiveData
import com.example.myapplication.data.daos.CategoryDao
import com.example.myapplication.models.Category

class CategoryLocalRepo(private val categoryDao: CategoryDao) : ICategoryRepo {
    override fun getAll(): LiveData<List<Category>> {
        return categoryDao.getAll()
    }

    override fun addCategory(category: Category) {
        categoryDao.addCategory(category)
    }

    override fun getCategory(id: Int): Category {
        return categoryDao.getCategory(id)
    }

    override fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    override fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }
}