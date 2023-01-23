package com.example.myapplication.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.models.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAll(): List<Category>

    @Query("SELECT * FROM categories WHERE id=:id LIMIT 1")
    fun getCategory(id: Int): Category

    @Update
    fun updateCategory(category: Category)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addCategory(category: Category)

    @Delete
    fun deleteCategory(category: Category)
}