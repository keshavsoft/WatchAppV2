package com.example.keshavsoftv2.presentation

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.example.keshavsoftv2.presentation.common.MessageBubble
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic

@Composable
fun WsScreenV5(isActive: Boolean) {

    val messages = remember { mutableStateListOf<String>() }
    val listState = rememberScalingLazyListState()

    /* 🔌 WebSocket lifecycle */
    LaunchedEffect(isActive) {
        if (isActive) VoiceWsClient.connect()
        else VoiceWsClient.close()
    }

    /* 📡 Incoming messages */
    LaunchedEffect(Unit) {
        VoiceWsClient.incomingMessages.collectLatest { msg ->
            messages.add(msg)
            listState.scrollToItem(messages.lastIndex)
        }
    }

    /* 🎤 Voice launcher */
    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText =
                result.data
                    ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?.firstOrNull()

            if (!spokenText.isNullOrBlank()) {
                VoiceWsClient.sendFinal(spokenText)
                messages.add("You: $spokenText")
            }
        }
    }

    /* 🖥️ UI */
    Scaffold(
        vignette = { Vignette(VignettePosition.TopAndBottom) },
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        }
    ) {

        Box(modifier = Modifier.fillMaxSize()) {

            /* 📨 Messages */
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

            /* 🎙️ Bottom Voice Button */
            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .size(ButtonDefaults.SmallButtonSize),
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
                    }
                    voiceLauncher.launch(intent)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice input"
                )
            }
        }
    }
}
