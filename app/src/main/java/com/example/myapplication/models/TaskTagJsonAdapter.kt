package com.example.myapplication.models

import kotlinx.serialization.Serializable

@Serializable
data class TaskTagJsonAdapter (
    var taskId: Int,
    var tagId: Int
)
{
    var id: Int = 0
}