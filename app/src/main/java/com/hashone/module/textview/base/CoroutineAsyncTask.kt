package com.hashone.module.textview.base

import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
abstract class CoroutineAsyncTask<Params, Progress, Result> {

    open fun onPreExecute() {}

    abstract fun doInBackground(vararg params: Params?): Result?

    open fun onProgressUpdate(vararg values: Progress?) {}

    open fun onPostExecute(result: Result?) {}

    open fun onCancelled(result: Result?) {}

    protected var isCancelled = false

    protected fun publishProgress(vararg progress: Progress?) {
        GlobalScope.launch(Dispatchers.Main) {
            onProgressUpdate(*progress)
        }
    }

    fun execute(vararg params: Params?) {

        onPreExecute()

        GlobalScope.launch(Dispatchers.Default) {
            val result = doInBackground(*params)

            withContext(Dispatchers.Main) {
                onPostExecute(result)
            }
        }
    }

    fun cancel(mayInterruptIfRunning: Boolean) {
        isCancelled = true
    }

}