package com.example.myapplication

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

class TasksListActivity : AppCompatActivity() {
    companion object {
        const val TASK: String = "task"
        const val NEW_TASK: String = "newTask"
        const val SEARCH_QUERY: String = "searchQuery"
    }

    private var searchQuery: String? = null

    private lateinit var recyclerView: RecyclerView
    private val adapter: TaskAdapter?
        get() = recyclerView.adapter as TaskAdapter?

    private lateinit var addButton: FloatingActionButton

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SEARCH_QUERY, searchQuery)
        super.onSaveInstanceState(outState)
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

        val repo = TaskRepository(TaskDatabase.getInstance(application).taskDao())
        val tasks = if (searchQuery != null) repo.findTasks(searchQuery!!) else repo.getAll
        tasks.observe(this@TasksListActivity) { tasks ->
            adapter!!.submitList(tasks)
        }

        searchView.setOnQueryTextListener(object : OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchQuery = newText
                val tasks = repo.findTasks(searchQuery!!)
                tasks.observe(this@TasksListActivity) { tasks ->
                    adapter!!.submitList(tasks)
                }

                return true
            }
        })

        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks_list)

        if (savedInstanceState != null)
            searchQuery = savedInstanceState.getString(SEARCH_QUERY)

        recyclerView = findViewById(R.id.recycler_view)
        addButton = findViewById(R.id.add_task)

        addButton.setOnClickListener {
            val intent = Intent(this@TasksListActivity, TaskActivity::class.java)
            intent.putExtra(NEW_TASK, true)
            startActivity(intent)
        }

        val repo = TaskRepository(TaskDatabase.getInstance(application).taskDao())
        recyclerView.adapter = TaskAdapter()

        repo.getAll.observe(this) { tasks ->
            adapter!!.submitList(tasks)
        }
    }

    // task_fragment.xml
    inner class TaskAdapter :
        RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
        private val dataSet = mutableListOf<Task>()
        private var editing = false
        private var count = 0

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

            private fun addCheckBox() {
                if (checkBox != null)
                    return

                checkBox = CheckBox(this@TasksListActivity)
                view.setOnClickListener {
                    checkBox!!.isChecked = !checkBox!!.isChecked
                }
                checkBox!!.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked)
                        count++
                    else
                        count--

                    if (count == 0) {
                        editing = false
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

            private fun firstCheckBox() {
                addCheckBox()
                checkBox!!.isChecked = true
            }

            fun setEvents(task: Task) {
                if (editing) {
                    addCheckBox()
                    return
                }

                removeCheckBox()
                view.setOnClickListener {
                    val intent = Intent(this@TasksListActivity, TaskActivity::class.java)
                    intent.putExtra(TASK, task.id)
                    startActivity(intent)
                }
                view.setOnLongClickListener {
                    editing = true
                    firstCheckBox()
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