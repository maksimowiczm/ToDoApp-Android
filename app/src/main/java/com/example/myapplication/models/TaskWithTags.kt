package com.example.myapplication.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation


data class TaskWithTags (
    @Embedded
    var task:Task,
    @Relation(
        parentColumn = "taskId",
        entityColumn = "tagId",
        associateBy = Junction(TaskTagCrossRef::class)
    )
    var tags: List<Tag>
)