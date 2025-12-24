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

@Composable
fun MessageBubble(
    text: String,
    isIncoming: Boolean
) {
    val bgColor =
        if (isIncoming) Color(0xFF2A2A2A) else Color(0xFF1E88E5)

    val alignment =
        if (isIncoming) Alignment.CenterStart else Alignment.CenterEnd

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = bgColor,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.body2,
                color = Color.White
            )
        }
    }
}
