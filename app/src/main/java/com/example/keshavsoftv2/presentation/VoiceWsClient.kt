package com.example.keshavsoftv2.presentation

import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*

object VoiceWsClient {

    private const val TAG = "VoiceWsClient"
    private const val WS_URL = "wss://keshavsoft.com/"

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    private val _incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<String> = _incomingMessages

    private val request = Request.Builder().url(WS_URL).build()

    private val listener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket connected")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            _incomingMessages.tryEmit(text)
        }

        override fun onFailure(
            webSocket: WebSocket,
            t: Throwable,
            response: Response?
        ) {
            Log.e(TAG, "WebSocket failed", t)
            this@VoiceWsClient.webSocket = null

            // 🔁 Auto-reconnect
            Handler(Looper.getMainLooper()).postDelayed({
                connect()
            }, 2000)
        }
    }

    fun connect() {
        if (webSocket != null) return
        webSocket = client.newWebSocket(request, listener)
    }

    fun close() {
        webSocket?.close(1000, "Client closing")
        webSocket = null
    }

    fun sendFinal(text: String) {
        if (text.isBlank()) return
        webSocket?.send("$text")
    }
}
