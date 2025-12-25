package com.example.keshavsoftv2.presentation

import WsScreenV9ViewModel
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
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WsScreenV9(
    isActive: Boolean,
    viewModel: WsScreenV9ViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val listState = rememberScalingLazyListState()

    val context = LocalContext.current
    val view = LocalView.current
    val controllerHolder = remember {
        mutableStateOf<VoiceInputControllerV7?>(null)
    }

    /* ───────── ACTIVITY LAUNCHERS ───────── */

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        controllerHolder.value?.onVoiceResult(result)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    /* ───────── CONTROLLER (LIFECYCLE SAFE) ───────── */

    val voiceController = remember {
        VoiceInputControllerV7(
            context = context,
            view = view,
            voiceLauncher = voiceLauncher,
            permissionLauncher = permissionLauncher
        ) { spokenText ->
            viewModel.onSpokenText(spokenText)
        }
    }
    LaunchedEffect(Unit) {
        controllerHolder.value = VoiceInputControllerV7(
            context = context,
            view = view,
            voiceLauncher = voiceLauncher,
            permissionLauncher = permissionLauncher
        ) { spokenText ->
            viewModel.onSpokenText(spokenText)
        }
    }

    /* ───────── SIDE EFFECTS ───────── */

    LaunchedEffect(Unit) {
        viewModel.startCollectingMessages()
    }

    LaunchedEffect(isActive) {
        viewModel.onActiveChanged(isActive)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.lastIndex)
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
                onClick = { controllerHolder.value?.handleMicClick() }
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Voice input")
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
