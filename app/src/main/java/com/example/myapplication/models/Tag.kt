package com.example.myapplication.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "tags")
data class Tag(
    var title: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    @Ignore
    var checked: Boolean = false
}

