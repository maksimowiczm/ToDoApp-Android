package com.example.myapplication

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.*
import android.widget.SearchView.OnQueryTextListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

class TasksListActivity : AppCompatActivity() {
    companion object {
        const val TASK = "task"
        const val NEW_TASK = "newTask"
        const val SEARCH_QUERY = "searchQuery"
        const val EDITING = "editing"
        const val TASKS_TO_DELETE = "tasksToDelete"
        const val REST = "restAPI"
        const val REST_AVAILABLE = "restAvailable"
    }

    private var rest: Boolean = false
    private var restAvailable: Boolean = false
    private lateinit var repo: ITaskRepository
    private var searchQuery: String? = null

    private lateinit var recyclerView: RecyclerView
    private val adapter: TaskAdapter?
        get() = recyclerView.adapter as TaskAdapter?

    private var editing = false
    private lateinit var floatingButton: FloatingActionButton
    private lateinit var cloudButton: FloatingActionButton
    private var tasksToDelete = ArrayList<Task>()
    private var tasksToDeleteIds = ArrayList<Int>()

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent = result.data ?: return@registerForActivityResult

                val edited = data.getBooleanExtra(TaskActivity.EDITED, false)
                if (edited) {
                    val id = data.getIntExtra(TaskActivity.ID, -1)
                    var title = data.getStringExtra(TaskActivity.TITLE)
                    val desc = data.getStringExtra(TaskActivity.DESC)
                    if (title == "")
                        title = null

                    thread {
                        val text =
                            if (id == -1) {
                                repo.addTask(Task(title, desc!!))
                                getString(R.string.task_added)
                            } else {
                                val task = repo.getTask(id)
                                task.title = title
                                task.desc = desc!!
                                repo.updateTask(task)
                                getString(R.string.task_saved)
                            }

                        runOnUiThread {
                            Toast.makeText(application, text, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

    private fun launchTaskActivity(new: Boolean, id: Int?) {
        val intent = Intent(this@TasksListActivity, TaskActivity::class.java)
        intent.putExtra(NEW_TASK, new)
        if (id != null)
            intent.putExtra(TASK, id)
        intent.putExtra(REST, rest)
        resultLauncher.launch(intent)
    }

    private fun setFloatingButton() {
        val color = if (editing) {
            floatingButton.setImageResource(R.drawable.ic_delete)
            ContextCompat.getColor(this, R.color.yellow)
        } else {
            floatingButton.setImageResource(R.drawable.ic_add)
            ContextCompat.getColor(this, R.color.floating)
        }

        floatingButton.backgroundTintList = ColorStateList.valueOf(color)

        if (floatingButton.hasOnClickListeners())
            return

        floatingButton.setOnClickListener {
            if (!editing) {
                launchTaskActivity(true, null)
                return@setOnClickListener
            }

            thread {
                for (task in tasksToDelete) {
                    repo.deleteTask(task)
                }

                val count = tasksToDelete.size.toString()
                tasksToDelete.clear()

                runOnUiThread {
                    val str = getString(R.string.task_deleted) + " " + count + " " +
                            resources.getQuantityString(
                                R.plurals.tasks_deleted,
                                tasksToDelete.size
                            )

                    Toast.makeText(application, str, Toast.LENGTH_SHORT).show()
                    editing = false
                    setFloatingButton()
                    adapter!!.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SEARCH_QUERY, searchQuery)
        outState.putBoolean(EDITING, editing)
        outState.putIntegerArrayList(
            TASKS_TO_DELETE,
            ArrayList(tasksToDelete.map { t -> t.id })
        )
        outState.putBoolean(REST, rest)
        super.onSaveInstanceState(outState)
    }

    private fun setObserver() {
        val tasks =
            if (searchQuery == "" || searchQuery == null) repo.getAll()
            else repo.findTasks(searchQuery!!)
        tasks.observe(this@TasksListActivity) { tasks ->
            adapter!!.submitList(tasks)
        }
    }

    override fun onResume() {
        super.onResume()
        setObserver()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search_task).actionView as SearchView
        searchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }

        if (searchQuery != null) {
            searchView.isIconified = true
            searchView.onActionViewExpanded()
            searchView.setQuery(searchQuery, false)
            searchView.isFocusable = true
        }

        searchView.setOnQueryTextListener(object : OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchQuery = newText
                setObserver()
                return true
            }
        })

        return true
    }

    private fun setCloudButtonStyle() {
        if(!restAvailable)
            return

        val color = if (rest) {
            cloudButton.setImageResource(R.drawable.ic_cloudon)
            ContextCompat.getColor(this, R.color.yellow)
        } else {
            cloudButton.setImageResource(R.drawable.ic_cloudoff)
            ContextCompat.getColor(this, R.color.gray)
        }

        cloudButton.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun setCloudButton() {
        if(!restAvailable)
            return

        cloudButton.visibility = View.VISIBLE
        setCloudButtonStyle()

        if (cloudButton.hasOnClickListeners())
            return

        cloudButton.setOnClickListener {
            rest = !rest
            setCloudButtonStyle()

            repo =
                if (rest) TaskRestRepo.getInstance()
                else TaskLocalRepository(TaskDatabase.getInstance(application).taskDao())

            setObserver()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks_list)

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(SEARCH_QUERY)
            editing = savedInstanceState.getBoolean(EDITING)
            rest = savedInstanceState.getBoolean(REST)
            tasksToDeleteIds =
                savedInstanceState.getIntegerArrayList(TASKS_TO_DELETE) as ArrayList<Int>
        }

        cloudButton = findViewById(R.id.cloud_task)
        restAvailable = intent.getBooleanExtra(REST_AVAILABLE, false)
        if (restAvailable)
            setCloudButton()

        repo =
            if (rest) TaskRestRepo.getInstance()
            else TaskLocalRepository(TaskDatabase.getInstance(application).taskDao())

        recyclerView = findViewById(R.id.recycler_view)
        floatingButton = findViewById(R.id.add_task)

        setFloatingButton()
        recyclerView.adapter = TaskAdapter()

        if (searchQuery != null) {
            setObserver()
            return
        }

        setObserver()
    }

    // task_fragment.xml
    inner class TaskAdapter :
        RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
        private val dataSet = mutableListOf<Task>()

        fun submitList(newData: List<Task>) {
            dataSet.clear()
            dataSet.addAll(newData)
            notifyDataSetChanged()
        }

        inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.title)
            val date: TextView = view.findViewById(R.id.date)

            var layout: LinearLayout = view.findViewById(R.id.linearLayout)
            private var checkBox: CheckBox? = null

            private fun addCheckBox(task: Task) {
                if (checkBox != null)
                    return

                checkBox = CheckBox(this@TasksListActivity)
                val color = ContextCompat.getColor(this@TasksListActivity, R.color.yellow)
                checkBox!!.buttonTintList = ColorStateList.valueOf(color)

                if (tasksToDelete.contains(task))
                    checkBox!!.isChecked = true

                view.setOnClickListener {
                    checkBox!!.isChecked = !checkBox!!.isChecked
                }

                checkBox!!.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked)
                        tasksToDelete.add(task)
                    else
                        tasksToDelete.remove(task)


                    if (tasksToDelete.size == 0) {
                        editing = false
                        setFloatingButton()
                        notifyDataSetChanged()
                    }
                }

                layout.addView(checkBox)
            }

            private fun removeCheckBox() {
                if (checkBox == null)
                    return

                layout.removeView(checkBox)
                checkBox = null
            }

            fun setEvents(task: Task) {
                if (editing) {
                    addCheckBox(task)
                    return
                }

                if(restAvailable)
                    cloudButton.visibility = View.VISIBLE

                removeCheckBox()
                view.setOnClickListener {
                    launchTaskActivity(false, task.id)
                }
                view.setOnLongClickListener {
                    editing = true
                    tasksToDelete.add(task)
                    cloudButton.visibility = View.INVISIBLE
                    setFloatingButton()
                    notifyDataSetChanged()
                    true
                }
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater
                .from(viewGroup.context)
                .inflate(R.layout.task_fragment, viewGroup, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val task = dataSet[position]

            viewHolder.title.text = task.getHeader()

            // XD
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy, H:mm ")
            viewHolder.date.text =
                task.date
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .toJavaLocalDateTime()
                    .format(formatter)

            viewHolder.setEvents(dataSet[position])
        }

        override fun getItemCount() = dataSet.size
    }
}