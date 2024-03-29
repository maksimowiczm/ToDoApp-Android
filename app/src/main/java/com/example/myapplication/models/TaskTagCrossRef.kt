package com.example.myapplication.models

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "tasktags",
    primaryKeys = ["taskId","tagId"])
data class TaskTagCrossRef(
    var taskId: Int,
    var tagId: Int
)

