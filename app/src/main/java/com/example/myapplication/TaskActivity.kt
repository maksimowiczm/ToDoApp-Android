package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.widget.addTextChangedListener
import kotlin.concurrent.thread

class TaskActivity : AppCompatActivity() {
    private lateinit var editTitle: EditText
    private lateinit var editDesc: EditText
    private lateinit var saveButton: MenuItem

    private var edited: Boolean = false
    private var rest: Boolean? = null
    lateinit var repo: ITaskRepository

    private var defaultTitle: String = ""
    private var defaultDesc: String = ""
    private var id: Int = -1
    private var title: String = ""
    private var desc: String = ""

    private lateinit var task: Task

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.task_menu, menu)
        saveButton = menu.findItem(R.id.save_task)
        saveButton.setOnMenuItemClickListener {
            finish()
            true
        }

        return true
    }

    private fun setFloatingColor() {
        val color =
            if (title == defaultTitle && desc == defaultDesc)
                ContextCompat.getColor(this, R.color.white)
            else
                ContextCompat.getColor(this, R.color.red)

        saveButton.icon.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            color,
            BlendModeCompat.SRC_ATOP
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        editTitle = findViewById(R.id.task_title)
        editDesc = findViewById(R.id.task_desc)

        rest = intent.getBooleanExtra(TasksListActivity.REST, false)
        repo = if (rest!!) {
            TaskRestRepo.getInstance()
        } else {
            TaskLocalRepository(TaskDatabase.getInstance(application).taskDao())
        }

        thread {
            id = intent.getIntExtra(TasksListActivity.TASK, -1)
            if (id != -1) {
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

    private fun saveTask() {
        thread {
            val newTitle: String? = if (title == "") null else title

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

        val toastText =
            if (id == -1) getString(R.string.task_added)
            else getString(R.string.task_saved)
        Toast.makeText(application, toastText, Toast.LENGTH_SHORT).show()
    }
}