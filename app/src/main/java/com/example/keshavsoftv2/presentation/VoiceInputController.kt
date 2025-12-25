package com.example.keshavsoftv2.presentation.voice

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.core.content.ContextCompat

class VoiceInputController(
    private val context: Context,
    private val view: View,
    private val voiceLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    private val permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    private val onResult: (String) -> Unit
) {

    /* ───────── PUBLIC API ───────── */

    fun handleMicClick() {
        if (hasMicPermission()) {
            // 🔔 HAPTIC — START LISTENING
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            launchVoiceInput()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            // 🔔 HAPTIC — START LISTENING
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            launchVoiceInput()
        }
    }

    fun onVoiceResult(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) return

        val spokenText =
            result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()

        if (!spokenText.isNullOrBlank()) {
            // 🔔 HAPTIC — STOP / RESULT
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onResult(spokenText)
        }
    }

    /* ───────── INTERNAL ───────── */

    private fun hasMicPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

    private fun launchVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
        }
        voiceLauncher.launch(intent)
    }
}
