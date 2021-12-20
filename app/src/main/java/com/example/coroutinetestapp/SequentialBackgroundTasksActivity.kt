package com.example.coroutinetestapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_parrallel_background_tasks.*
import kotlinx.android.synthetic.main.activity_parrallel_background_tasks.button
import kotlinx.android.synthetic.main.activity_parrallel_background_tasks.text
import kotlinx.android.synthetic.main.activity_sequential_background_tasks.*
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class SequentialBackgroundTasksActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "SequentialBackgroundTas"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sequential_background_tasks)

        button.setOnClickListener {
            fakeRequestApi()
        }
    }

    private fun fakeRequestApi() {
        CoroutineScope(Dispatchers.IO).launch {
            val executionTime = measureTimeMillis {
                val result1 = async {
                    Log.d(TAG, "Launching job#1 with: ${Thread.currentThread().name}")
                    getResult1FromApi()
                }.await()

                val result2 = async {
                    Log.d(TAG, "Launching job#2 with: ${Thread.currentThread().name}")
                    getResult2FromApi(result1)
                }.await()

                Log.d(TAG, "Got Result2: $result2")
            }
            Log.d(TAG, "Total elapsed time: $executionTime ms.")
        }
    }

    private suspend fun setTextOnMainThread(input: String) {
        withContext(Dispatchers.Main) {
            setNewText(input)
        }
    }

    private fun setNewText(input: String) {
        val newText = text.text.toString() + "\n$input"
        text.text = newText
    }

    private suspend fun getResult1FromApi(): String {
        delay(1000)
        return "Result #1"
    }

    private suspend fun getResult2FromApi(result1: String): String {
        delay(1700)
        return if (result1 == "Result #1") {
            "Result #2"
        } else {
            "Result #1 was incorrect..."
        }
    }
}