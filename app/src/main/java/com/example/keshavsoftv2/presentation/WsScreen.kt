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
fun WsScreen(isActive: Boolean) {

    var latestMessage by remember { mutableStateOf("Waiting for data...") }

    // ✅ Connect ONLY when active
    LaunchedEffect(isActive) {
        if (isActive) {
            VoiceWsClient.connect()
        }
    }

    // ✅ Collect messages
    LaunchedEffect(Unit) {
        VoiceWsClient.incomingMessages.collectLatest { msg ->
            latestMessage = msg
        }
    }

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
