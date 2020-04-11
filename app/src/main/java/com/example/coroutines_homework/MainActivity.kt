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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.InputStream


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
        Log.d("Attention", "Run coroutine with IO context")
        CoroutineScope(IO).launch {

            val url = getRandomCatUrl()
            var bitmapFromUrl = getBitmapFromUrl(url)
            updateUI(bitmapFromUrl)
        }

    }

    private fun getRandomCatUrl(): String {
        Log.d("Attention", "Getting url")

        val client1 = OkHttpClient();

        val request = Request.Builder()
            .addHeader(CUSTOME_HEADER_KEY, CUSTOME_HEADER_VALUE)
            .url(BASE_URL)
            .build()

        var execute: Response = client1.newCall(request).execute()
        var responseBody = execute.body?.string()

        val split = responseBody?.split(",")
        var url = ""
        for (str in split!!) {
            if (str.contains("url")) {
                url = str.split(":")[1]
                url += ":" + str.split(":")[2]
                break
            }
        }

        return url.replace("\"", "")

    }

    private fun getBitmapFromUrl(url: String): Bitmap? {
        Log.d("Attention", "Getting Bitmap")
        val client1 = OkHttpClient();

        val request = Request.Builder()
            .addHeader(CUSTOME_HEADER_KEY, CUSTOME_HEADER_VALUE)
            .url(url)
            .build()

        var response = client1.newCall(request).execute()
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
