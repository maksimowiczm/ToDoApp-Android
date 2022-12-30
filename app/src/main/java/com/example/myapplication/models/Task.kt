package com.example.myapplication.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "tasks")
data class Task(
    var title: String? = null,
    var desc: String,
    var date: Instant = Clock.System.now()
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    fun getHeader(): String {
        return if (title == null) desc else title!!
    }
}

