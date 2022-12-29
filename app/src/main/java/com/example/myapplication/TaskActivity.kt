package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.widget.addTextChangedListener
import kotlin.concurrent.thread

class TaskActivity : AppCompatActivity() {
    companion object {
        const val EDITED = "edited"
        const val TITLE = "title"
        const val DESC = "desc"
        const val ID = "id"
    }

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
        setSaveIconColor()
        saveButton.setOnMenuItemClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(TITLE, title)
            resultIntent.putExtra(DESC, desc)
            resultIntent.putExtra(EDITED, edited)
            resultIntent.putExtra(ID, id)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
            true
        }

        return true
    }

    private fun setSaveIconColor() {
        val color =
            if (edited)
                ContextCompat.getColor(this, R.color.red)
            else
                ContextCompat.getColor(this, R.color.white)

        saveButton.icon.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            color,
            BlendModeCompat.SRC_ATOP
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        if (savedInstanceState != null) {
            edited = savedInstanceState.getBoolean(EDITED)
            title = savedInstanceState.getString(TITLE)!!
            desc = savedInstanceState.getString(DESC)!!
        }

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

                if (!edited) {
                    runOnUiThread {
                        editTitle.setText(task.title)
                        editDesc.setText(task.desc)

                        title = task.title ?: ""
                        defaultTitle = title
                        desc = task.desc
                        defaultDesc = desc
                    }
                } else {
                    runOnUiThread {
                        editTitle.setText(title)
                        editDesc.setText(desc)

                        defaultTitle = task.title ?: ""
                        defaultDesc = task.desc
                    }
                }

                runOnUiThread {
                    editTitle.setSelection(editTitle.text.length)
                    editDesc.setSelection(editDesc.text.length)
                }
            }

            runOnUiThread {
                editTitle.addTextChangedListener {
                    title = editTitle.text.toString()
                    checkIfEdited()
                    setSaveIconColor()
                }
                editDesc.addTextChangedListener {
                    desc = editDesc.text.toString()
                    checkIfEdited()
                    setSaveIconColor()
                }
            }
        }
    }

    private fun checkIfEdited() {
        if (title != defaultTitle) {
            edited = true
            return
        }
        if (desc != defaultDesc) {
            edited = true
            return
        }

        edited = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EDITED, edited)
        outState.putString(TITLE, title)
        outState.putString(DESC, desc)
        super.onSaveInstanceState(outState)
    }
}