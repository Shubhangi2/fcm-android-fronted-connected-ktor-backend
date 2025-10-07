package com.example.fcm_learning

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fcm_learning.retrofit.FcmApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import okio.IOException
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class ChatViewModel : ViewModel() {
    var state by mutableStateOf(ChatState())
        private set

    companion object {
        private const val TAG = "CHAT_API"
    }

    private class DetailedLoggingInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()

            Log.d(TAG, "=========================== REQUEST ===========================")
            Log.d(TAG, "URL: ${request.url}")
            Log.d(TAG, "Method: ${request.method}")
            Log.d(TAG, "Protocol: ${request.url.scheme}")
            Log.d(TAG, "Host: ${request.url.host}")
            Log.d(TAG, "Port: ${request.url.port}")
            Log.d(TAG, "Path: ${request.url.encodedPath}")
            Log.d(TAG, "Query: ${request.url.query}")

            // Log ALL headers
            Log.d(TAG, "Headers:")
            request.headers.names().forEach { name ->
                Log.d(TAG, "  $name: ${request.header(name)}")
            }

            // Log request body
            request.body?.let { body ->
                val buffer = Buffer()
                body.writeTo(buffer)
                Log.d(TAG, "Request Body: ${buffer.readUtf8()}")
                Log.d(TAG, "Content-Type: ${body.contentType()}")
                Log.d(TAG, "Content-Length: ${body.contentLength()}")
            } ?: Log.d(TAG, "Request Body: null")

            // Execute the request
            val startTime = System.currentTimeMillis()
            val response = chain.proceed(request)
            val endTime = System.currentTimeMillis()

            Log.d(TAG, "=========================== RESPONSE ===========================")
            Log.d(TAG, "Response Code: ${response.code}")
            Log.d(TAG, "Response Message: ${response.message}")
            Log.d(TAG, "Response Time: ${endTime - startTime}ms")

            // Log response headers
            Log.d(TAG, "Response Headers:")
            response.headers.names().forEach { name ->
                Log.d(TAG, "  $name: ${response.header(name)}")
            }

            // Log response body
            val responseBody = response.body
            return if (responseBody != null) {
                val responseString = responseBody.string()
                Log.d(TAG, "Response Body: $responseString")
                Log.d(TAG, "Response Content-Type: ${responseBody.contentType()}")
                Log.d(TAG, "==============================================================")
                // Create new response with the consumed body
                response.newBuilder()
                    .body(responseString.toResponseBody(responseBody.contentType()))
                    .build()
            } else {
                Log.d(TAG, "==============================================================")
                response
            }
        }
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(DetailedLoggingInterceptor())
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val api: FcmApi = Retrofit.Builder()
        .baseUrl("http://192.168.29.169:8080")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()
        .create(FcmApi::class.java)

    fun onRemoteTokenChange(newToken: String) {
        state = state.copy(
            remoteToken = newToken
        )
    }

    fun onSubmitRemoteToken() {
        state = state.copy(
            isEnteringToken = false
        )
    }

    fun onMessagechange(message: String) {
        state = state.copy(
            messageText = message
        )
    }

    fun sendMessage(isBoradcast: Boolean) {
        viewModelScope.launch {
            val messageDto = SendMessageDto(
                to = if (isBoradcast) null else state.remoteToken,
                notification = NotificationBody(
                    title = "New Message",
                    body = state.messageText
                )
            )
            try {
                if (isBoradcast) {
                    api.broadcast(messageDto)
                } else {
                    api.sendMessage(messageDto)
                }

                state = state.copy(messageText = "")
            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}