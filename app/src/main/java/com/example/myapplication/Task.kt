package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: Int,
    val title: String? = null,
    val desc: String,
    val date: Instant = Clock.System.now()
) {
    fun getHeader(): String {
        if (title != null)
            return title

        return desc
    }
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return Instant.fromEpochMilliseconds(value!!)
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilliseconds()
    }
}
