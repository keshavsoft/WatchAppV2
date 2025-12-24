package com.example.keshavsoftv2.presentation

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberScalingLazyListState
import kotlinx.coroutines.flow.collectLatest

@Composable
fun WsScreenV1(isActive: Boolean) {
    MaterialTheme {

        val messages = remember { mutableStateListOf<String>() }
        val listState = rememberScalingLazyListState()

        LaunchedEffect(isActive) {
            if (isActive) {
                VoiceWsClient.connect()
            }
        }


        // 📡 Collect ALL incoming messages
        LaunchedEffect(Unit) {
            VoiceWsClient.incomingMessages.collectLatest { msg ->
                messages.add(msg)
            }
        }

        // ⌚ Wear OS list
        ScalingLazyColumn(
            modifier = Modifier,
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(messages.size) { index ->
                Text(
                    text = messages[index],
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}
