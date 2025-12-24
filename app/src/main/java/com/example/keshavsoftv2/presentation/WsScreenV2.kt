package com.example.keshavsoftv2.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.example.keshavsoftv2.presentation.common.MessageBubble
import kotlinx.coroutines.flow.collectLatest
@Composable
fun WsScreenV2(isActive: Boolean) {

    val messages = remember { mutableStateListOf<String>() }
    val listState = rememberScalingLazyListState()

    // 🔌 Connect only when page is active
    LaunchedEffect(isActive) {
        if (isActive) {
            VoiceWsClient.connect()
        }
    }

    // 📡 COLLECT MESSAGES (THIS WAS MISSING / WRONG)
    LaunchedEffect(Unit) {
        VoiceWsClient.incomingMessages.collectLatest { msg ->
            messages.add(msg)
        }
    }

    // ⬇️ Auto-scroll
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.lastIndex)
        }
    }

    // 🖥️ UI
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(messages.size) { index ->
            MessageBubble(
                text = messages[index],
                isIncoming = true
            )
        }
    }
}
