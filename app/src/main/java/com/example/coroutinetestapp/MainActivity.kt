package com.example.coroutinetestapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val PROGRESS_MAX = 100
        private const val PROGRESS_START = 0
        private const val JOB_TIME = 4000
    }

    private lateinit var job: CompletableJob

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        job_button.setOnClickListener {
            if (!::job.isInitialized) {
                initJob()
            }
            job_progress_bar.startOrCancelJob(job)
        }
        open_parallel_background_tasks_activity_button.setOnClickListener {
            startActivity(Intent(this, ParallelBackgroundTasksActivity::class.java))
        }
        open_sequential_background_tasks_activity_button.setOnClickListener {
            startActivity(Intent(this, SequentialBackgroundTasksActivity::class.java))
        }
    }

    private fun initJob() {
        job_button.text = "Start Job #1"
        updateJobCompleteTextView("")
        job = Job()
        job.invokeOnCompletion {
            it?.message.let {
                var msg = it
                if (msg.isNullOrEmpty()) {
                    msg = "Unknown cancellation"
                }
                Log.d(TAG, "$job was cancelled. Reason: $msg")
                showToast(msg)
            }
        }
        job_progress_bar.progress = PROGRESS_START
        job_progress_bar.max = PROGRESS_MAX
    }

    private fun ProgressBar.startOrCancelJob(job: Job) {
        if (this.progress > 0) {
            Log.d(TAG, "$job is already active. Cancelling...")
            resetJob()
        } else {
            job_button.setText("Cancel Job #1")
            CoroutineScope(Dispatchers.IO + job).launch {
                Log.d(TAG, "Coroutine $this is activated with job: $job")
                for (i in PROGRESS_START..PROGRESS_MAX) {
                    delay((JOB_TIME / PROGRESS_MAX).toLong())
                    this@startOrCancelJob.progress = i
                }
                updateJobCompleteTextView("Job is complete")
            }
        }
    }

    private fun resetJob() {
        if (job.isActive || job.isCompleted) {
            job.cancel(CancellationException("Resetting job"))
        }
        initJob()
    }

    private fun updateJobCompleteTextView(text: String) {
        GlobalScope.launch(Dispatchers.Main) {
            job_complete_text.text = text
        }
    }

    private fun showToast(text: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
        }
    }
}