package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.repos.TaskRestRepo
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.setDisplayShowTitleEnabled(false)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this@MainActivity, TasksListActivity::class.java)

        thread {
            // hehe
            // Thread.sleep(250)
            val restAvailable = try {
                TaskRestRepo.getInstance().getStatus()
            } catch (e: Exception) {
                false
            }

            intent.putExtra(TasksListActivity.REST_AVAILABLE, restAvailable)

            runOnUiThread {
                startActivity(intent)
                finish()
            }
        }
    }
}