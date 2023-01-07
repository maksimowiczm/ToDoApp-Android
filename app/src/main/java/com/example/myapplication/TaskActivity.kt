package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.widget.addTextChangedListener
import com.example.myapplication.models.Task
import com.example.myapplication.data.*
import com.example.myapplication.data.repos.*
import com.example.myapplication.models.Category
import com.example.myapplication.models.Tag
import kotlin.concurrent.thread

class TaskActivity : AppCompatActivity() {
    companion object {
        const val EDITED = "edited"
        const val TITLE = "title"
        const val DESC = "desc"
        const val ID = "id"
        const val CATEGORY_ID = "category_id"
        const val TAGS_ID = "tags_id"
    }

    private lateinit var editTitle: EditText
    private lateinit var editDesc: EditText
    private lateinit var saveButton: MenuItem
    private lateinit var categorySpinner: Spinner
    private lateinit var tagsButton: Button
    private lateinit var tagsTextView: TextView

    private var edited: Boolean = false
    private var rest: Boolean? = null
    lateinit var taskRepo: ITaskRepo
    lateinit var categoryRepo: ICategoryRepo
    lateinit var tagRepo: ITagRepo
    lateinit var taskTagRepo: ITaskTagCrossRefRepo

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
    private var defaultTagsId: ArrayList<Int> = ArrayList()
    private var tagsId: ArrayList<Int> = ArrayList()
    private var tags: List<Tag> = emptyList()
    private lateinit var tagsCheckedArr: BooleanArray
    private lateinit var tagsTitlesArr: Array<String>

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
            resultIntent.putExtra(TAGS_ID, tagsId)
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
            tagsId = savedInstanceState.getIntegerArrayList(TAGS_ID) as ArrayList<Int>
        }

        editTitle = findViewById(R.id.task_title)
        editDesc = findViewById(R.id.task_desc)
        categorySpinner = findViewById(R.id.task_category_spinner)
        tagsTextView = findViewById(R.id.task_tags_text)
        tagsButton = findViewById(R.id.task_tags_button)

        rest = intent.getBooleanExtra(TasksListActivity.REST, false)
        taskRepo = if (rest!!) {
            TaskRestRepo.getInstance()
        } else {
            TaskLocalRepo(ToDoDatabase.getInstance(application).taskDao())
        }

        categoryRepo = CategoryLocalRepo(ToDoDatabase.getInstance(application).categoryDao())
        tagRepo = TagLocalRepo(ToDoDatabase.getInstance(application).tagDao())
        taskTagRepo = TaskTagCrossRefLocalRepo(ToDoDatabase.getInstance(application).taskTagCrossRefDao())

        thread {
            id = intent.getIntExtra(TasksListActivity.TASK, -1)
            categories = categoryRepo.getAll()
            tags = tagRepo.getAll()
            if (id != -1) {
                task = taskRepo.getTask(id)

                if (!edited) {
                    tagsId = ArrayList(taskTagRepo.getTaskTagCrossRefForTask(id).map{it.tagId})
                    defaultTagsId = ArrayList(tagsId)
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
                    defaultTagsId = ArrayList(taskTagRepo.getTaskTagCrossRefForTask(id).map{it.tagId})
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

            addCategorySpinner()

            addTagsDialog()
        }
    }

    private fun addCategorySpinner() {
        //inicjalizacja spinnera z kategoriami
        runOnUiThread {
            categoriesTitles = categories.map { it.title }
            val adapter = ArrayAdapter(this, R.layout.spinner_category, categoriesTitles)
            categorySpinner.adapter = adapter

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
            if (categoryId != -1) {
                val idx =
                    categoriesTitles.indexOf(categories.find { it.id == categoryId }?.title)
                categorySpinner.setSelection(idx)
            }
        }
    }

    private fun addTagsDialog() {
        //inicjalizacja selectlista do tagów
        tags.filter { tag -> tagsId.contains(tag.id) }
            .forEach { it.checked = true }

        tagsCheckedArr = tags.map { it.checked }.toBooleanArray()
        tagsTitlesArr = tags.map { it.title }.toTypedArray()

        var tmpString = ""
        tagsId.forEach {
            tmpString = tmpString + " " + (tags.find { tag -> tag.id == it }?.title ?: "Błąd")
        }

        if (tmpString != "") {
            runOnUiThread {
                tagsTextView.text = tmpString
            }
        }

        runOnUiThread {
            tagsButton.setOnClickListener {
                val adBuilder = AlertDialog.Builder(this@TaskActivity)
                adBuilder.setTitle(R.string.avilable_tags)

                adBuilder.setMultiChoiceItems(tagsTitlesArr, tagsCheckedArr)
                    { _, idx, isChecked -> tagsCheckedArr[idx] = isChecked }

                adBuilder.setPositiveButton("OK") { _, _ ->
                    tagsTextView.text = ""
                    tagsTextView.hint = getString(R.string.tag_text_hint)
                    var tmpTagString = ""
                    val tmpTagsId = ArrayList<Int>()

                    for (i in tagsCheckedArr.indices) {
                        tags[i].checked = tagsCheckedArr[i]
                        if (!tagsCheckedArr[i]) continue
                        tmpTagString = tmpTagString + " " + tagsTitlesArr[i]
                        tmpTagsId.add(tags[i].id)
                    }
                    if (tmpTagString!="") {
                        tagsTextView.text = tmpTagString
                    }
                    tagsId = tmpTagsId
                    checkIfEdited()
                }
                adBuilder.setNeutralButton("Cancel") { _, _ ->
                    tagsCheckedArr = tags.map { it.checked }.toBooleanArray()
                }
                val dialog = adBuilder.create()
                dialog.show()
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
        if (tagsId != defaultTagsId) {
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
        outState.putIntegerArrayList(TAGS_ID, tagsId)
        super.onSaveInstanceState(outState)
    }
}