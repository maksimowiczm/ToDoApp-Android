package com.example.myapplication

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
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
    }

    private lateinit var recyclerView: RecyclerView
    private val adapter: TaskAdapter?
        get() = recyclerView.adapter as TaskAdapter?

    private lateinit var addButton: FloatingActionButton

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search_task).actionView as SearchView
        searchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }

        searchView.setOnQueryTextListener(object : OnQueryTextListener {
            val repo = TaskRepository(TaskDatabase.getInstance(application).taskDao())

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText != null) {
                    val tasks = repo.findTasks(newText!!)
                    tasks.observe(this@TasksListActivity) { tasks ->
                        adapter!!.submitList(tasks)
                    }
                }
                return true
            }
        })

        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks_list)

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

        fun submitList(newData: List<Task>) {
            dataSet.clear()
            dataSet.addAll(newData)
            notifyDataSetChanged()
        }

        inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.title)
            val date: TextView = view.findViewById(R.id.date)

            fun setOnClick(task: Task) {
                view.setOnClickListener {
                    val intent = Intent(this@TasksListActivity, TaskActivity::class.java)
                    intent.putExtra(TASK, task.id)
                    startActivity(intent)
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

            viewHolder.setOnClick(dataSet[position])
        }

        override fun getItemCount() = dataSet.size
    }
}