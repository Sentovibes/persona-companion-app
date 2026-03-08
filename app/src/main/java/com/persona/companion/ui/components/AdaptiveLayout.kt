package com.persona.companion.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.persona.companion.utils.DeviceType
import com.persona.companion.utils.rememberDeviceType

/**
 * Adaptive layout that changes based on device type
 */
@Composable
fun AdaptiveDetailLayout(
    modifier: Modifier = Modifier,
    statsContent: @Composable ColumnScope.() -> Unit,
    imageContent: @Composable () -> Unit = {}
) {
    val deviceType = rememberDeviceType()
    
    when (deviceType) {
        DeviceType.PHONE -> {
            // Phone: Vertical layout, stats only (no image)
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column {
                        statsContent()
                    }
                }
            }
        }
        
        DeviceType.TABLET -> {
            // Tablet: L-shaped layout (stats left, image right)
            Row(
                modifier = modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Stats column (left side)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column {
                            statsContent()
                        }
                    }
                }
                
                // Image column (right side)
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                        .padding(vertical = 24.dp, horizontal = 24.dp)
                ) {
                    imageContent()
                }
            }
        }
        
        DeviceType.TV, DeviceType.CAST -> {
            // TV: Full-right image with stats on left
            Row(
                modifier = modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(48.dp)
            ) {
                // Stats column (left side, narrower)
                LazyColumn(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(48.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        Column {
                            statsContent()
                        }
                    }
                }
                
                // Image column (right side, larger)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(vertical = 48.dp, horizontal = 48.dp)
                ) {
                    imageContent()
                }
            }
        }
    }
}

/**
 * Adaptive grid for game selection
 */
@Composable
fun AdaptiveGameGrid(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val deviceType = rememberDeviceType()
    
    when (deviceType) {
        DeviceType.PHONE -> {
            // Phone: Vertical list
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
        
        DeviceType.TABLET, DeviceType.TV, DeviceType.CAST -> {
            // Tablet/TV: Horizontal row
            LazyRow(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

/**
 * Adaptive series selection layout
 */
@Composable
fun AdaptiveSeriesLayout(
    modifier: Modifier = Modifier,
    seriesCards: @Composable () -> Unit
) {
    val deviceType = rememberDeviceType()
    
    when (deviceType) {
        DeviceType.PHONE -> {
            // Phone: Vertical stack
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                seriesCards()
            }
        }
        
        DeviceType.TABLET, DeviceType.TV, DeviceType.CAST -> {
            // Tablet/TV: Horizontal row
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                seriesCards()
            }
        }
    }
}
