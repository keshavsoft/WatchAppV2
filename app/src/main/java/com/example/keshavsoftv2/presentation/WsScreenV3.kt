package com.example.keshavsoftv2.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.RemoteInput
import androidx.wear.compose.material.*

import com.example.keshavsoftv2.presentation.common.MessageBubble

import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.compose.material.ScalingLazyListState

import kotlinx.coroutines.flow.collectLatest

@Composable
fun WsScreenV3(isActive: Boolean) {

    val messages = remember { mutableStateListOf<String>() }
    val listState = rememberScalingLazyListState()
    var inputText by remember { mutableStateOf("") }

    // 🔌 WebSocket lifecycle
    LaunchedEffect(isActive) {
        if (isActive) VoiceWsClient.connect()
    }

    // 📡 Incoming messages
    LaunchedEffect(Unit) {
        VoiceWsClient.incomingMessages.collectLatest { msg ->
            messages.add(msg)
            listState.scrollToItem(messages.lastIndex)
        }
    }

    // 🎤 Remote input launcher
    val inputLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val reply = RemoteInput.getResultsFromIntent(result.data)
                ?.getCharSequence("wear_input")
                ?.toString()
            if (!reply.isNullOrBlank()) inputText = reply
        }
    }


    Scaffold(
        vignette = { Vignette(VignettePosition.TopAndBottom) },
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        }
    ) {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // ✅ SINGLE list
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(bottom = 70.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(messages.size) { index ->
                    MessageBubble(
                        text = messages[index],
                        isIncoming = !messages[index].startsWith("You:")
                    )
                }
            }


            // ⌨️ Bottom input
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 🔹 LINE 1: Text input chip
                CompactChip(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    label = {
                        Text(
                            if (inputText.isEmpty()) "Tap to type"
                            else inputText,
                            maxLines = 1
                        )
                    },
                    onClick = {
                        val remoteInput = RemoteInput.Builder("wear_input")
                            .setLabel("Reply")
                            .build()

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                        }

                        RemoteInput.addResultsToIntent(
                            arrayOf(remoteInput),
                            intent,
                            Bundle()
                        )

                        inputLauncher.launch(intent)
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 🔹 LINE 2: Send button
                Button(
                    modifier = Modifier.size(ButtonDefaults.SmallButtonSize),
                    onClick = {
                        if (inputText.isNotBlank()) {
                            VoiceWsClient.sendFinal(inputText)
                            messages.add("You: $inputText")
                            inputText = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send"
                    )
                }
            }


        }
    }



}
