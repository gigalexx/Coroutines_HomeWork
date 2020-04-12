package com.example.coroutines_homework

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class MainActivity : AppCompatActivity() {

    private val BASE_URL = "https://api.thecatapi.com/v1/images/search"
    private val CUSTOME_HEADER_KEY = "x-api-key"
    private val CUSTOME_HEADER_VALUE = "7d32c131-e5a3-4e02-8a31-689b67ab2aa3"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            Log.d("Attention", "Click on button")
            makeApiRequest()
        }
    }


    private fun makeApiRequest() {
        val startTime = System.currentTimeMillis()
        Log.d("Attention", "Run coroutine with IO context")
        val parentJob = CoroutineScope(IO).launch {
            val deferred = async { getRandomCatUrl() } // async request
            val bitmap = getBitmapFromUrl(deferred.await())     // sync request
            updateUI(bitmap)                                              // UI update
        }

        parentJob.invokeOnCompletion {
            println("Total elapsed time: ${System.currentTimeMillis() - startTime} ")
        }
    }

    private suspend fun getRandomCatUrl(): String {
        return suspendCoroutine { continuation ->
            Log.d("Attention", "Getting url")
            var url: String
            val client = OkHttpClient()
            val request = Request.Builder()
                .addHeader(CUSTOME_HEADER_KEY, CUSTOME_HEADER_VALUE)
                .url(BASE_URL)
                .build()
            client.run {
                newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        println("Something went wrong :(")
                        continuation.resumeWithException(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        val split = responseBody?.split(",")

                        for (str in split!!) {
                            if (str.contains("url")) {
                                url = str.split(":")[1]
                                url += ":" + str.split(":")[2]
                                url = url.replace("\"", "")
                                continuation.resume(url)
                                break
                            }
                        }
                    }
                })
            }
        }
    }

    private fun getBitmapFromUrl(url: String): Bitmap? {
        Log.d("Attention", "Getting Bitmap")
        Log.d("Attention", "Bitmap url: $url")

        val client1 = OkHttpClient()

        val request = Request.Builder()
            .addHeader(CUSTOME_HEADER_KEY, CUSTOME_HEADER_VALUE)
            .url(url)
            .build()

        val response = client1.newCall(request).execute()
        val inputStream: InputStream? = response.body?.byteStream()
        return BitmapFactory.decodeStream(inputStream)
    }


    private suspend fun updateUI(result: Bitmap?) {
        Log.d("Attention", "Updating UI element through coroutine with MAIN context")
        withContext(Main) {
            imageView.setImageBitmap(result)
        }
    }
}
