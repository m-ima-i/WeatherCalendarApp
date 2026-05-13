package com.anri.weathercalendarapp.core

import android.util.Log
import com.anri.weathercalendarapp.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.nio.charset.Charset

class ApiLoggingInterceptor(
    private val tagSuffix: String
) : Interceptor {

    private val tag = "ApiLog_$tagSuffix"

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!BuildConfig.DEBUG) {
            return chain.proceed(chain.request())
        }

        val request = chain.request()

        Log.d(tag, "→ ${request.method} ${request.url}")
        request.headers.forEach { (name, value) ->
            val displayValue = if (name.equals("Authorization", ignoreCase = true)) "<redacted>" else value
            Log.d(tag, "→ Header: $name: $displayValue")
        }
        request.body?.let { body ->
            val buffer = okio.Buffer()
            body.writeTo(buffer)
            val requestBody = buffer.readString(Charset.forName("UTF-8"))
            if (requestBody.isNotEmpty()) {
                Log.d(tag, "→ Body: $requestBody")
            }
        }

        val response = chain.proceed(request)
        val responseBody = response.body
        val responseString = responseBody?.string() ?: ""

        Log.d(tag, "← ${response.code} ${request.url}")
        if (responseString.isNotEmpty()) {
            Log.d(tag, "← Body: $responseString")
        }

        val newResponseBody = responseString.toResponseBody(responseBody?.contentType())
        return response.newBuilder().body(newResponseBody).build()
    }
}
