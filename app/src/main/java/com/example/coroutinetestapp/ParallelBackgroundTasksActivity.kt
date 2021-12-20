package com.example.coroutinetestapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_parrallel_background_tasks.*
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class ParallelBackgroundTasksActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "ParallelBackgroundTasks"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parrallel_background_tasks)

        button.setOnClickListener {
            setNewText("Clicked!")
//            fakeApiRequest()
            fakeApiRequestUsingAsyncAndAwait()
        }
    }

    private fun fakeApiRequest() {
        val startTime = System.currentTimeMillis()
        val parentJob = GlobalScope.launch(Dispatchers.IO) {
            val job1 = launch {
                val time1 = measureTimeMillis {
                    Log.d(TAG, "launching job#1 in thread ${Thread.currentThread().name}")
                    val result1 = getResult1FromApi()
                    setTextOnMainThread("Got $result1")
                }
                Log.d(TAG, "Completed job#1 in $time1 ms.")
            }

            val job2 = launch {
                val time2 = measureTimeMillis {
                    Log.d(TAG, "launching job#2 in thread ${Thread.currentThread().name}")
                    val result2 = getResult2FromApi()
                    setTextOnMainThread("Got $result2")
                }
                Log.d(TAG, "Completed job#2 in $time2 ms.")
            }
        }
        parentJob.invokeOnCompletion {
            Log.d(TAG, "Total elapsed time ${System.currentTimeMillis() - startTime} ms.")
        }
    }

    private fun fakeApiRequestUsingAsyncAndAwait() {
       CoroutineScope(Dispatchers.IO).launch {
           val executionTime = measureTimeMillis {
               val result1: Deferred<String> = async {
                   Log.d(TAG, "Launching Job#1 with: ${Thread.currentThread().name}")
                   getResult1FromApi()
               }
               val result2: Deferred<String> = async {
                   Log.d(TAG, "Launching job#2 with: ${Thread.currentThread().name}")
                   getResult2FromApi()
               }
               Log.d(TAG, "Sherif_1")
               setTextOnMainThread("Got ${result1.await()}")
               Log.d(TAG, "Sherif_2")
               setTextOnMainThread("Got ${result2.await()}")
               Log.d(TAG, "Sherif_3")
           }
           Log.d(TAG, "Total elapsed time: $executionTime ms.")
       }
    }

    private suspend fun setTextOnMainThread(input: String) {
        withContext(Dispatchers.Main) {
            setNewText(input)
        }
    }

    private fun setNewText(input: String ){
        val newText = text.text.toString() + "\n$input"
        text.text = newText
    }

    private suspend fun getResult1FromApi(): String {
        delay(1000)
        return "Result #1"
    }

    private suspend fun getResult2FromApi(): String {
        delay(1700)
        return "Result #2"
    }
}