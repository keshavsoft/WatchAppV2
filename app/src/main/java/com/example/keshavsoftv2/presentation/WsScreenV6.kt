package com.example.keshavsoftv2.presentation

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import com.example.keshavsoftv2.presentation.common.MessageBubble
import com.example.keshavsoftv2.presentation.voice.VoiceInputController
import kotlinx.coroutines.flow.collectLatest

import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView

@Composable
fun WsScreenV6(isActive: Boolean) {

    /* ───────── STATE ───────── */

    val messages = remember { mutableStateListOf<String>() }
    val listState = rememberScalingLazyListState()
    val context = LocalContext.current

    /* ───────── WEBSOCKET ───────── */

    LaunchedEffect(isActive) {
        if (isActive) VoiceWsClient.connect()
        else VoiceWsClient.close()
    }

    LaunchedEffect(Unit) {
        VoiceWsClient.incomingMessages.collectLatest { msg ->
            messages.add(msg)
            listState.scrollToItem(messages.lastIndex)
        }
    }

    /* ───────── VOICE CONTROLLER SETUP ───────── */

    lateinit var voiceController: VoiceInputController

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        voiceController.onVoiceResult(result)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        voiceController.onPermissionResult(granted)
    }

    val view = LocalView.current

    voiceController = remember {
        VoiceInputController(
            context = context,
            view = view,
            voiceLauncher = voiceLauncher,
            permissionLauncher = permissionLauncher
        ) { spokenText ->
            sendMessage(spokenText, messages)
        }
    }



    /* ───────── UI ───────── */

    Scaffold(
        vignette = { Vignette(VignettePosition.TopAndBottom) },
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        }
    ) {

        Box(modifier = Modifier.fillMaxSize()) {

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

            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .size(ButtonDefaults.SmallButtonSize),
                onClick = voiceController::handleMicClick
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice input"
                )
            }
        }
    }
}

/* ───────── MESSAGE HELPER ───────── */

private fun sendMessage(
    text: String,
    messages: MutableList<String>
) {
    VoiceWsClient.sendFinal(text)
    messages.add("You: $text")
}
