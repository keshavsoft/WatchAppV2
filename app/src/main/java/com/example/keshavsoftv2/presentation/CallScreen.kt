package com.example.keshavsoftv1.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.example.keshavsoftv2.R

import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip

// ---------- PAGE 0 : Call Screen ----------

@Composable
fun CallScreen(
    onClose: () -> Unit,
    showSwipeHint: Boolean = false
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val phoneNumber = stringResource(id = R.string.call_number)

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "callButtonScale"
    )

    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(160)
            pressed = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-14).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    pressed = true
                    context.startActivity(
                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                    )
                },
                modifier = Modifier
                    .size(88.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale),
                shape = CircleShape,
                colors = ButtonDefaults.primaryButtonColors(
                    backgroundColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                )
            ) {
                Text(text = "📞", fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Call $phoneNumber",
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        Chip(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClose()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(120.dp)
                .padding(bottom = 18.dp),
            label = { Text("Close", fontSize = 11.sp) },
            icon = { Text("❌") },
            shape = RoundedCornerShape(50),
            colors = ChipDefaults.secondaryChipColors(
                backgroundColor = Color(0xFFB00020),
                contentColor = Color.White
            )
        )
    }
}

// ---------- PAGE 1 : Greetings Screen ----------

@Composable
fun InfoScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Greetings from", fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(id = R.string.app_name),
                        fontSize = 16.sp
                    )
        }
    }
}

// ---------- PAGER : Swipe between screens ----------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPager(onClose: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    Box(modifier = Modifier.fillMaxSize()) {

        // ----- PAGES -----
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> CallScreen(onClose = onClose, showSwipeHint = true)
                1 -> InfoScreen()
            }
        }

        // ----- TOP DOTS INDICATOR -----
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { index ->
                val selected = pagerState.currentPage == index

                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .size(if (selected) 6.dp else 4.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected)
                                Color.White.copy(alpha = 0.9f)
                            else
                                Color.LightGray.copy(alpha = 0.6f)
                        )
                )
            }
        }
    }
}
