package com.example.keshavsoftv2.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RootPager(onClose: () -> Unit) {

    val pagerState = rememberPagerState(pageCount = { 5 })

    Box(modifier = Modifier.fillMaxSize()) {

        // ----- PAGES -----
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> CallScreen(onClose = onClose, showSwipeHint = true)
                1 -> InfoScreen()
                2 -> WsScreenV6(isActive = pagerState.currentPage == 2)
                3 -> WsScreen(isActive = pagerState.currentPage == 3)
                4 -> WsScreenV1(isActive = pagerState.currentPage == 4)
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
