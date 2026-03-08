package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.persona.companion.data.SeriesData
import com.persona.companion.models.PersonaSeries
import com.persona.companion.navigation.Screen
import com.persona.companion.ui.components.AdaptiveSeriesLayout
import com.persona.companion.ui.theme.*
import com.persona.companion.utils.DeviceType
import com.persona.companion.utils.rememberContentPadding
import com.persona.companion.utils.rememberDeviceType
import com.persona.companion.utils.rememberTextScaleFactor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val deviceType = rememberDeviceType()
    val contentPadding = rememberContentPadding()
    val textScale = rememberTextScaleFactor()
    
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = contentPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "PERSONA",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = (42.sp * textScale),
                    letterSpacing = 6.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = TextPrimary
            )
            Text(
                text = "COMPANION",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = (MaterialTheme.typography.labelLarge.fontSize * textScale),
                    letterSpacing = 10.sp
                ),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(if (deviceType == DeviceType.TV) 80.dp else 56.dp))

            // Series cards - adaptive layout
            AdaptiveSeriesLayout(
                modifier = Modifier.fillMaxWidth()
            ) {
                SeriesData.allSeries.forEach { series ->
                    SeriesCard(
                        series = series,
                        deviceType = deviceType,
                        onClick = {
                            navController.navigate(Screen.GameSelection.createRoute(series.id))
                        },
                        modifier = when (deviceType) {
                            DeviceType.PHONE -> Modifier.fillMaxWidth()
                            DeviceType.TABLET -> Modifier.weight(1f)
                            DeviceType.TV, DeviceType.CAST -> Modifier.weight(1f)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (deviceType == DeviceType.TV) 48.dp else 32.dp))

            Text(
                text = "Community-driven • Open source",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = (MaterialTheme.typography.labelSmall.fontSize * textScale)
                ),
                color = TextDisabled
            )
        }
    }
}

@Composable
private fun SeriesCard(
    series: PersonaSeries,
    deviceType: DeviceType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(series.color, series.color.copy(alpha = 0.6f))
    )
    
    val cardHeight = when (deviceType) {
        DeviceType.PHONE -> 90.dp
        DeviceType.TABLET -> 120.dp
        DeviceType.TV, DeviceType.CAST -> 160.dp
    }
    
    val textScale = rememberTextScaleFactor()

    Box(
        modifier = modifier
            .height(cardHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(brush = gradient)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        // Decorative number in background
        Text(
            text = series.id.removePrefix("p"),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp),
            fontSize = (72.sp * textScale),
            fontWeight = FontWeight.ExtraBold,
            color = Color.White.copy(alpha = 0.12f)
        )

        Column(
            modifier = Modifier.padding(start = 24.dp)
        ) {
            Text(
                text = series.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = (MaterialTheme.typography.titleLarge.fontSize * textScale)
                ),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${series.games.size} game${if (series.games.size != 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (MaterialTheme.typography.bodyMedium.fontSize * textScale)
                ),
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
