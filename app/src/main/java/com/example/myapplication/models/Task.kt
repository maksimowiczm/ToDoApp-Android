package com.example.myapplication.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            childColumns = ["categoryId"],
            parentColumns = ["id"]
        )
    ],
    indices = [
        Index(value = ["categoryId"])
    ])
data class Task(
    var title: String? = null,
    var desc: String,
    var date: Instant = Clock.System.now(),
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var categoryId: Int? = null

    fun getHeader(): String {
        return if (title == null) desc else title!!
    }
}

