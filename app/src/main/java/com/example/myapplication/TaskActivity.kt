package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.widget.addTextChangedListener
import com.example.myapplication.models.Task
import com.example.myapplication.data.*
import com.example.myapplication.data.repos.*
import com.example.myapplication.models.Category
import kotlin.concurrent.thread

class TaskActivity : AppCompatActivity() {
    companion object {
        const val EDITED = "edited"
        const val TITLE = "title"
        const val DESC = "desc"
        const val ID = "id"
        const val CATEGORY_ID = "category_id"
    }

    private lateinit var editTitle: EditText
    private lateinit var editDesc: EditText
    private lateinit var saveButton: MenuItem
    private lateinit var categorySpinner: Spinner

    private var edited: Boolean = false
    private var rest: Boolean? = null
    lateinit var repo: ITaskRepo
    lateinit var categoryRepo: ICategoryRepo

    private var defaultTitle: String = ""
    private var defaultDesc: String = ""
    private var defaultCategoryId: Int = -1
    private var id: Int = -1
    private var title: String = ""
    private var desc: String = ""
    private var categoryId: Int = -1

    private lateinit var task: Task
    private lateinit var categories: List<Category>
    private lateinit var categoriesTitles: List<String>

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
            resultIntent.putExtra(CATEGORY_ID, categoryId)
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
            categoryId = savedInstanceState.getInt(CATEGORY_ID)
        }

        editTitle = findViewById(R.id.task_title)
        editDesc = findViewById(R.id.task_desc)
        categorySpinner = findViewById(R.id.task_category_spinner)

        rest = intent.getBooleanExtra(TasksListActivity.REST, false)
        repo = if (rest!!) {
            TaskRestRepo.getInstance()
        } else {
            TaskLocalRepo(ToDoDatabase.getInstance(application).taskDao())
        }

        categoryRepo = CategoryLocalRepo(ToDoDatabase.getInstance(application).categoryDao())

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
                        categoryId = task.categoryId ?: -1
                        defaultCategoryId = categoryId
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

            categories = categoryRepo.getAll()
            categoriesTitles = categories.map { it.title }

            val adapter = ArrayAdapter(this, R.layout.spinner_category, categoriesTitles)
            categorySpinner.adapter = adapter

            runOnUiThread {
                categorySpinner.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View, position: Int, id: Long
                    ) {
                        categoryId =
                            categories.find { it.title == categoriesTitles[position] }?.id ?: -1
                        checkIfEdited()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        categoryId = defaultCategoryId
                        checkIfEdited()
                    }


                }
                if (categoryId != -1){
                    val idx = categoriesTitles.indexOf(categories.find { it.id == categoryId }?.title)
                    categorySpinner.setSelection(idx)
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
        if (categoryId != defaultCategoryId) {
            edited = true
            return
        }
        edited = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EDITED, edited)
        outState.putString(TITLE, title)
        outState.putString(DESC, desc)
        outState.putInt(CATEGORY_ID, categoryId)
        super.onSaveInstanceState(outState)
    }
}