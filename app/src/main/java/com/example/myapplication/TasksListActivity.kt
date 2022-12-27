package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onResume() {
        super.onResume()

        adapter?.notifyDataSetChanged()
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

        repo.getAll.observe(this) { tasks ->
            if (adapter == null)
                recyclerView.adapter = TaskAdapter(tasks)
            else
                adapter!!.notifyDataSetChanged()
        }
    }

    // task_fragment.xml
    inner class TaskAdapter(private val dataSet: List<Task>) :
        RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

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