package com.example.myapplication.data.repos

import com.example.myapplication.models.Tag

interface ITagRepo {
    fun getAll(): List<Tag>
    fun addTag(tag: Tag)
    fun getTag(id: Int): Tag
    fun updateTag(tag: Tag)
    fun deleteTag(tag: Tag)
}