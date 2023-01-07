package com.example.myapplication.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.models.Tag

@Dao
interface TagDao {
    @Query("SELECT * FROM tags")
    fun getAll(): List<Tag>

    @Query("SELECT * FROM tags WHERE id=:id LIMIT 1")
    fun getTag(id: Int): Tag

    @Update
    fun updateTag(tag: Tag)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addTag(tag: Tag)

    @Delete
    fun deleteTag(tag: Tag)
}