package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import kotlin.concurrent.thread

class TaskActivity : AppCompatActivity() {
    private lateinit var editTitle: EditText
    private lateinit var editDesc: EditText

    private var edited: Boolean = false

    private var id: Int = -1
    private var title: String = ""
    private var desc: String = ""

    private lateinit var task: Task

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        editTitle = findViewById(R.id.task_title)
        editDesc = findViewById(R.id.task_desc)

        thread {
            id = intent.getIntExtra(TasksListActivity.TASK, -1)
            if (id != -1) {
                val repo = TaskRepository(TaskDatabase.getInstance(application).taskDao())
                task = repo.getTask(id)

                runOnUiThread {
                    editTitle.setText(task.title)
                    editDesc.setText(task.desc)

                    title = task.title ?: ""
                    desc = task.desc
                }
            }

            runOnUiThread {
                editTitle.addTextChangedListener {
                    edited = true
                    title = editTitle.text.toString()
                }
                editDesc.addTextChangedListener {
                    edited = true
                    desc = editDesc.text.toString()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        if (!edited)
            return

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
}