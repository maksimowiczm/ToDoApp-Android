package com.example.myapplication

import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.concurrent.thread

class TaskActivity : AppCompatActivity() {
    private lateinit var editTitle: EditText
    private lateinit var editDesc: EditText
    private lateinit var saveButton: FloatingActionButton

    private var edited: Boolean = false

    private var defaultTitle: String = ""
    private var defaultDesc: String = ""
    private var id: Int = -1
    private var title: String = ""
    private var desc: String = ""

    private lateinit var task: Task

    private fun setFloatingColor() {
        val color =
            if (title == defaultTitle && desc == defaultDesc)
                ColorStateList.valueOf(resources.getColor(R.color.floating))
            else
                ColorStateList.valueOf(resources.getColor(R.color.yellow))

        saveButton.backgroundTintList = color
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        editTitle = findViewById(R.id.task_title)
        editDesc = findViewById(R.id.task_desc)
        saveButton = findViewById(R.id.save_task)

        saveButton.setOnClickListener {
            finish()
        }

        thread {
            id = intent.getIntExtra(TasksListActivity.TASK, -1)
            if (id != -1) {
                val repo = TaskRepository(TaskDatabase.getInstance(application).taskDao())
                task = repo.getTask(id)

                runOnUiThread {
                    editTitle.setText(task.title)
                    editDesc.setText(task.desc)

                    title = task.title ?: ""
                    defaultTitle = title
                    desc = task.desc
                    defaultDesc = desc
                }
            }

            runOnUiThread {
                editTitle.addTextChangedListener {
                    edited = true
                    title = editTitle.text.toString()
                    setFloatingColor()
                }
                editDesc.addTextChangedListener {
                    edited = true
                    desc = editDesc.text.toString()
                    setFloatingColor()
                }
            }
        }
    }

    fun saveTask() {
        thread {
            val newTitle: String? = if (title == "") null else title
            val repo = TaskRepository(TaskDatabase.getInstance(application).taskDao())

            // id == -1 = nowy task
            if (id == -1) {
                repo.addTask(Task(newTitle, desc))
                return@thread
            }

            // edycja taska
            task.title = newTitle
            task.desc = desc
            repo.updateTask(task)
        }
    }

    override fun onPause() {
        super.onPause()

        if (!edited)
            return

        saveTask()
    }
}