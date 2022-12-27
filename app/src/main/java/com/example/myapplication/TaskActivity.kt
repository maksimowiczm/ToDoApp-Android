package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.core.widget.addTextChangedListener

class TaskActivity : AppCompatActivity() {
    private lateinit var editTitle: EditText
    private lateinit var editDesc: EditText

    private var edited: Boolean = false

    private var id: Int = -1
    private var title: String = ""
    private var desc: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        editTitle = findViewById(R.id.task_title)
        editDesc = findViewById(R.id.task_desc)

        id = intent.getIntExtra(TasksListActivity.TASK, -1)
        if (id != -1) {
            val task = TaskContainer.getInstance().getTask(id)
            editTitle.setText(task.title)
            editDesc.setText(task.desc)

            title = task.title ?: ""
            desc = task.desc
        }

        editTitle.addTextChangedListener {
            edited = true
            title = editTitle.text.toString()
        }
        editDesc.addTextChangedListener {
            edited = true
            desc = editDesc.text.toString()
        }
    }

    override fun onPause() {
        super.onPause()

        if (!edited)
            return

        val container = TaskContainer.getInstance()

        val newTitle: String? = if (title == "") null else title

        if (id == -1)
            container.addTask(Task(id, newTitle, desc))
        else
            container.editTask(Task(id, newTitle, desc))
    }
}