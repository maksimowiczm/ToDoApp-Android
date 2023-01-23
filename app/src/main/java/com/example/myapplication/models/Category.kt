package com.example.myapplication.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName="categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var title :String,
    var icon :String
    )