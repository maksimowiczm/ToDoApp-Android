package com.example.myapplication

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Task(val id: Int, val title: String? = null, val desc: String, val date: Instant = Clock.System.now()) {
    fun getHeader(): String {
        if (title != null)
            return title

        return desc
    }
}