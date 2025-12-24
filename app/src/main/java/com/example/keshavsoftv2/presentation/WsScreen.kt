package com.example.keshavsoftv2.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.MaterialTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun WsScreen() {

    var latestMessage by remember { mutableStateOf("Waiting for data...") }

    // 🔌 Connect & Disconnect WebSocket safely
    DisposableEffect(Unit) {
        VoiceWsClient.connect()

        onDispose {
            VoiceWsClient.close()
        }
    }

    // 📡 Collect messages from WebSocket
    LaunchedEffect(Unit) {
        VoiceWsClient.incomingMessages.collectLatest { msg ->
            latestMessage = msg
        }
    }

    // 🖥️ UI (Watch friendly)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = latestMessage,
            style = MaterialTheme.typography.body1
        )
    }
}
