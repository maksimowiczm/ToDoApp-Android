package com.example.myapplication

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.*
import android.widget.SearchView.OnQueryTextListener
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

    private var rest: Boolean = true
    private var restAvailable: Boolean = false
    lateinit var repo: ITaskRepository
    private var searchQuery: String? = null

    private lateinit var recyclerView: RecyclerView
    private val adapter: TaskAdapter?
        get() = recyclerView.adapter as TaskAdapter?

    private var editing = false
    private lateinit var floatingButton: FloatingActionButton
    private var tasksToDelete = ArrayList<Task>()
    private var tasksToDeleteIds = ArrayList<Int>()

    private fun launchTaskActivity(new: Boolean, id: Int?) {
        val intent = Intent(this@TasksListActivity, TaskActivity::class.java)
        intent.putExtra(NEW_TASK, new)
        if (id != null)
            intent.putExtra(TASK, id)
        intent.putExtra(REST, rest)
        startActivity(intent)
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

                runOnUiThread {
                    val str = getString(R.string.task_deleted) + " " +
                            tasksToDelete.size.toString() + " " +
                            resources.getQuantityString(R.plurals.tasks_deleted, tasksToDelete.size)

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
        outState.putIntegerArrayList(TASKS_TO_DELETE, ArrayList(tasksToDelete.map { t -> t.id }))
        super.onSaveInstanceState(outState)
    }

    private fun updateQuery() {
        if (searchQuery == null)
            return

        val tasks = if (searchQuery == "") repo.getAll() else repo.findTasks(searchQuery!!)
        tasks.observe(this@TasksListActivity) { tasks ->
            adapter!!.submitList(tasks)
        }
    }

    override fun onResume() {
        super.onResume()
        updateQuery()
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

        val tasks = if (searchQuery != null) repo.findTasks(searchQuery!!) else repo.getAll()
        tasks.observe(this@TasksListActivity) { tasks ->
            adapter!!.submitList(tasks)
        }

        searchView.setOnQueryTextListener(object : OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchQuery = newText
                updateQuery()
                return true
            }
        })

        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks_list)

        restAvailable = intent.getBooleanExtra(TasksListActivity.REST_AVAILABLE, false)

        repo = if (rest) {
            TaskRestRepo.getInstance()
        } else {
            TaskLocalRepository(TaskDatabase.getInstance(application).taskDao())
        }

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(SEARCH_QUERY)
            editing = savedInstanceState.getBoolean(EDITING)
            tasksToDeleteIds =
                savedInstanceState.getIntegerArrayList(TASKS_TO_DELETE) as ArrayList<Int>
        }

        recyclerView = findViewById(R.id.recycler_view)
        floatingButton = findViewById(R.id.add_task)

        setFloatingButton()
        recyclerView.adapter = TaskAdapter()

        repo.getAll().observe(this) { tasks ->
            for (task in tasks)
                if (tasksToDeleteIds.contains(task.id))
                    tasksToDelete.add(task)

            adapter!!.submitList(tasks)
        }
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
            var checkBox: CheckBox? = null

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

            private fun firstCheckBox(task: Task) {
                addCheckBox(task)
                checkBox!!.isChecked = true
            }

            fun setEvents(task: Task) {
                if (editing) {
                    addCheckBox(task)
                    return
                }

                removeCheckBox()
                view.setOnClickListener {
                    launchTaskActivity(false, task.id)
                }
                view.setOnLongClickListener {
                    editing = true
                    firstCheckBox(task)
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
            val formatter = DateTimeFormatter.ofPattern("d MMMM, H:mm")
            viewHolder.date.text =
                task.date.toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime().format(formatter)

            viewHolder.setEvents(dataSet[position])
        }

        override fun getItemCount() = dataSet.size
    }
}