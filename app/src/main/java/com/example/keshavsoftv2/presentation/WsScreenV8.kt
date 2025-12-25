package com.example.keshavsoftv2.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import com.example.keshavsoftv2.presentation.common.MessageBubble
import com.example.keshavsoftv2.presentation.voice.VoiceInputControllerV7
import kotlinx.coroutines.flow.collectLatest

@Composable
fun WsScreenV8(isActive: Boolean) {

    /* ───────── STATE ───────── */

    val messages = remember { mutableStateListOf<String>() }
    val listState = rememberScalingLazyListState()

    val context = LocalContext.current
    val view = LocalView.current

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

    /* ───────── CONTROLLER HOLDER (KEY FIX) ───────── */

    val voiceControllerState = remember {
        mutableStateOf<VoiceInputControllerV7?>(null)
    }

    /* ───────── ACTIVITY LAUNCHERS ───────── */

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        voiceControllerState.value?.onVoiceResult(result)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        voiceControllerState.value?.onPermissionResult(granted)
    }

    /* ───────── VOICE CONTROLLER (SAFE INIT) ───────── */

    LaunchedEffect(Unit) {
        voiceControllerState.value = VoiceInputControllerV7(
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

            /* 🎤 Mic Button */
            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .size(ButtonDefaults.SmallButtonSize),
                onClick = {
                    voiceControllerState.value?.handleMicClick()
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

/* ───────── MESSAGE HELPER ───────── */

private fun sendMessage(
    text: String,
    messages: MutableList<String>
) {
    VoiceWsClient.sendFinal(text)
    messages.add("You: $text")
}
