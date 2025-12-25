package com.example.keshavsoftv2.presentation

import WsScreenV13ViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import com.example.keshavsoftv2.presentation.common.MessageBubble
import com.example.keshavsoftv2.presentation.voice.VoiceInputControllerV7

@Composable
fun WsScreenV13(
    isActive: Boolean,
    viewModel: WsScreenV13ViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val listState = rememberScalingLazyListState()

    val context = LocalContext.current
    val view = LocalView.current
    val controllerHolder = remember { mutableStateOf<VoiceInputControllerV7?>(null) }

    /* ───────── ACTIVITY LAUNCHERS ───────── */

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        controllerHolder.value?.onVoiceResult(result)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    /* ───────── VOICE CONTROLLER ───────── */

    val voiceController = remember {
        VoiceInputControllerV7(
            context = context,
            view = view,
            voiceLauncher = voiceLauncher,
            permissionLauncher = permissionLauncher
        ) { spokenText ->
            viewModel.onUserSpoken(spokenText)
        }
    }

    LaunchedEffect(Unit) {
        controllerHolder.value = voiceController
        viewModel.startListeningOnce()
    }

    LaunchedEffect(isActive) {
        viewModel.onScreenActive(isActive)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.lastIndex)
        }
    }

    /* ───────── UI DELEGATION ───────── */

    WsChatContent(
        messages = messages,
        listState = listState,
        onMicClick = {
            controllerHolder.value?.handleMicClick()
        }
    )
}
