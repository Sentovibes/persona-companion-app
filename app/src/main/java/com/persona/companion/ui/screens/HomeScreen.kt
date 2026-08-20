package com.persona.companion.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.persona.companion.R
import com.persona.companion.data.SeriesData
import com.persona.companion.models.PersonaSeries
import com.persona.companion.navigation.Screen
import com.persona.companion.ui.components.AdaptiveSeriesLayout
import com.persona.companion.ui.components.CastButton
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
                    // Cast button (only on phone/tablet, not TV)
                    if (deviceType != DeviceType.TV) {
                        CastButton()
                    }
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
                    fontSize = 42.sp,
                    letterSpacing = 6.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = TextPrimary
            )
            Text(
                text = "COMPANION",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 10.sp
                ),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(if (deviceType == DeviceType.TV) 60.dp else 40.dp))
            
            // Quick access buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickAccessButton(
                    icon = Icons.Default.Favorite,
                    label = "Favorites",
                    onClick = { navController.navigate(Screen.Favorites.route) },
                    modifier = Modifier.weight(1f)
                )
                QuickAccessButton(
                    icon = Icons.Default.History,
                    label = "Recent",
                    onClick = { navController.navigate(Screen.RecentlyViewed.route) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(if (deviceType == DeviceType.TV) 40.dp else 24.dp))

            // Series cards - adaptive layout
            AdaptiveSeriesLayout(
                modifier = Modifier.fillMaxWidth()
            ) {
                SeriesData.allSeries.forEach { series ->
                    SeriesCard(
                        series = series,
                        deviceType = deviceType,
                        onSeriesClick = {
                            navController.navigate(Screen.GameSelection.createRoute(series.id))
                        },
                        onGameClick = { gameId ->
                            navController.navigate(Screen.Category.createRoute(series.id, gameId))
                        },
                        modifier = when (deviceType) {
                            DeviceType.PHONE -> Modifier.fillMaxWidth()
                            DeviceType.TABLET -> Modifier.weight(1f)
                            DeviceType.TV, DeviceType.CAST -> Modifier.weight(1f)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (deviceType == DeviceType.TV) 40.dp else 24.dp))

            Text(
                text = "Community-driven • Open source",
                style = MaterialTheme.typography.labelSmall,
                color = TextDisabled
            )
        }
    }
}

@Composable
private fun SeriesCard(
    series: PersonaSeries,
    deviceType: DeviceType,
    onSeriesClick: () -> Unit,
    onGameClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(series.color, series.color.copy(alpha = 0.65f))
    )
    
    val cardHeight = when (deviceType) {
        DeviceType.PHONE -> 124.dp
        DeviceType.TABLET -> 140.dp
        DeviceType.TV, DeviceType.CAST -> 165.dp
    }

    val logoRes = when (series.id) {
        "p3" -> R.drawable.p3r_logo
        "p4" -> R.drawable.p4g_logo
        "p5" -> R.drawable.p5r_logo
        else -> null
    }

    Box(
        modifier = modifier
            .height(cardHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(brush = gradient)
            .clickable(onClick = onSeriesClick),
        contentAlignment = Alignment.CenterStart
    ) {
        // P5 Star decoration in top left
        if (series.id == "p5") {
            Text(
                text = "★",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 6.dp)
                    .rotate(-14f),
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold
            )
        }

        // Decorative number in background
        Text(
            text = series.id.removePrefix("p"),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 28.dp),
            fontSize = if (deviceType == DeviceType.PHONE) 76.sp else 92.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White.copy(alpha = 0.14f)
        )

        // Chevron icon on right edge
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .size(28.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 18.dp, top = 12.dp, bottom = 12.dp, end = 48.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Official Logo Image or Title fallback
            if (logoRes != null) {
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = series.title,
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.CenterStart,
                    modifier = Modifier
                        .height(if (deviceType == DeviceType.PHONE) 46.dp else 54.dp)
                        .fillMaxWidth(0.72f)
                )
            } else {
                Text(
                    text = series.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick-launch game chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                series.games.forEach { game ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.Black.copy(alpha = 0.30f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                        modifier = Modifier.clickable {
                            onGameClick(game.id)
                        }
                    ) {
                        Text(
                            text = getGameShortTitle(game.id),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun getGameShortTitle(gameId: String): String = when (gameId) {
    "p3r" -> "P3R"
    "p3fes" -> "P3 FES"
    "p3p" -> "P3P"
    "p4g" -> "P4G"
    "p4" -> "P4"
    "p5r" -> "P5R"
    "p5" -> "P5"
    else -> gameId.uppercase()
}

@Composable
private fun QuickAccessButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textScale = rememberTextScaleFactor()
    
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
