package com.graphql.zegnus.graphqlapplication

import android.support.annotation.WorkerThread
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.graphql.zegnus.graphqlapplication.api.ApiResponse
import com.graphql.zegnus.graphqlapplication.api.Book
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException

class BookViewModel {

    private val okHttpClient = OkHttpClient.Builder().build()
    private val job = Job()
    private val uiScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.Main + job)

    fun requestBookId(id: String, callback: (Feedback) -> Unit) {
        uiScope.launch {
            callback(Feedback.Loading)

            val bookRequest = withContext(Dispatchers.IO) { request(id) }

            callback(bookRequest)
        }
    }

    @WorkerThread
    fun request(id: String): Feedback {

        val json = MediaType.parse("application/json; charset=utf-8")
        val query = """
            {"query":"{ book(id: \"$id\") { id, name, genre } }"}
        """
        val body = RequestBody.create(json, query)

        val request = Request.Builder()
            .url("http://10.0.2.2:4000/graphql/")
            .post(body)
            .build()

        return try {
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                parseResponse(response)
            } else {
                Feedback.Error(response.message())
            }
        } catch (exception: IOException) {
            Feedback.Error(exception.localizedMessage)
        }
    }

    private fun parseResponse(response: Response): Feedback {
        val body = response.body() ?: return Feedback.Error("body is null")

        val stringJson = body.string()
        val mapper = ObjectMapper().registerModule(KotlinModule())
        return try {
            val apiData = mapper.readValue<ApiResponse>(stringJson)
            Feedback.Loaded(apiData.data.book)
        } catch (e: Exception) {
            Feedback.Error("error parsing response -> ${e.message}")
        }
    }

    fun stop() {
        job.cancel()
    }

    sealed class Feedback {
        object Loading : Feedback()
        data class Error(val message: String) : Feedback()
        data class Loaded(val book: Book) : Feedback()
    }
}
