package com.example.myapplication.data.repos

import androidx.lifecycle.LiveData
import com.example.myapplication.data.daos.TagDao
import com.example.myapplication.models.Tag

class TagLocalRepo(private val tagDao: TagDao) : ITagRepo {
    override fun getAll(): LiveData<List<Tag>> {
        return tagDao.getAll()
    }

    override fun addTag(tag: Tag) {
        tagDao.addTag(tag)
    }

    override fun getTag(id: Int): Tag {
        return tagDao.getTag(id)
    }

    override fun updateTag(tag: Tag) {
        tagDao.updateTag(tag)
    }

    override fun deleteTag(tag: Tag) {
        tagDao.deleteTag(tag)
    }
}