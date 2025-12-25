package com.example.keshavsoftv2.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.RemoteInput
import androidx.wear.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import kotlinx.coroutines.flow.collectLatest
import com.example.keshavsoftv2.presentation.common.MessageBubble

@Composable
fun WsScreenV4(isActive: Boolean) {

    /* ---------- STATE ---------- */

    val messages = remember { mutableStateListOf<String>() }
    val listState = rememberScalingLazyListState()
    var inputText by remember { mutableStateOf("") }

    /* ---------- WEBSOCKET ---------- */

    LaunchedEffect(isActive) {
        if (isActive) {
            VoiceWsClient.connect()
        }
    }

    LaunchedEffect(Unit) {
        VoiceWsClient.incomingMessages.collectLatest { msg ->
            messages.add(msg)
            listState.scrollToItem(messages.lastIndex)
        }
    }

    /* ---------- REMOTE INPUT ---------- */

    val inputLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val reply = RemoteInput.getResultsFromIntent(result.data)
                ?.getCharSequence("wear_input")
                ?.toString()

            if (!reply.isNullOrBlank()) {
                inputText = reply
            }
        }
    }

    /* ---------- UI ---------- */

    Scaffold(
        vignette = { Vignette(VignettePosition.TopAndBottom) },
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        }
    ) {

        Box(modifier = Modifier.fillMaxSize()) {

            /* ----- MESSAGE LIST ----- */

            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(messages.size) { index ->
                    MessageBubble(
                        text = messages[index],
                        isIncoming = !messages[index].startsWith("You:")
                    )
                }
            }

            /* ----- BOTTOM INPUT (2 LINES) ----- */

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // LINE 1 — INPUT FIELD (RemoteInput)
                OutlinedChip(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    label = {
                        Text(
                            text = if (inputText.isEmpty()) "Type message"
                            else inputText,
                            maxLines = 1
                        )
                    },
                    onClick = {
                        val remoteInput = RemoteInput.Builder("wear_input")
                            .setLabel("Type message")
                            .build()

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            addCategory(Intent.CATEGORY_DEFAULT)
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

                // LINE 2 — SEND BUTTON
                Button(
                    modifier = Modifier.size(ButtonDefaults.SmallButtonSize),
                    enabled = inputText.isNotBlank(),
                    onClick = {
                        VoiceWsClient.sendFinal(inputText)
                        messages.add("You: $inputText")
                        inputText = ""
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
