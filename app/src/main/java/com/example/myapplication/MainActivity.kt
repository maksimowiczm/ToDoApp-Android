package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.setDisplayShowTitleEnabled(false)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this@MainActivity, TasksListActivity::class.java)

        thread {
            val container = TaskContainer.getInstance()
            if (container.apiRequests == 0)
                container.addTasksFromApi()

            // hehe
            Thread.sleep(250)

            runOnUiThread {
                startActivity(intent)
                finish()
            }
        }
    }
}