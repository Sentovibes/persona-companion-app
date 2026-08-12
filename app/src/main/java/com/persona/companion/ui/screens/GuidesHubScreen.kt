package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.persona.companion.data.SeriesData
import com.persona.companion.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidesHubScreen(
    seriesId: String,
    gameId: String,
    onBack: () -> Unit,
    onNavigateToQuestGuide: (giver: String) -> Unit,
    onNavigateToBossGuide: () -> Unit,
    onNavigateToDayGuide: () -> Unit
) {
    val series = remember(seriesId) { SeriesData.findSeries(seriesId) }
    val game = remember(gameId) { SeriesData.findGame(seriesId, gameId) }
    val accentColor = series?.color ?: Persona3Blue

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Guides & Walkthroughs", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(game?.title ?: "", color = TextSecondary, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Plan your year, prep for bosses, and clear every side-quest — no spreadsheets required.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Day-by-Day Calendar Guide (available for all games)
            item {
                GuideHubCard(
                    title = "Day-by-Day Guide",
                    subtitle = "What to do on each day: deadlines, exams, unlocks, and free-time tips, month by month.",
                    icon = Icons.Default.CalendarMonth,
                    accentColor = accentColor,
                    onClick = onNavigateToDayGuide
                )
            }

            // Game-Specific Quest Guides
            when (gameId) {
                "p3fes", "p3p", "p3r" -> {
                    item {
                        GuideHubCard(
                            title = if (gameId == "p3p") "Elizabeth & Theodore's Requests" else "Elizabeth's Requests",
                            subtitle = "Fusion walkthroughs, item hunt locations, and combat challenges.",
                            icon = Icons.Default.MenuBook,
                            accentColor = accentColor,
                            onClick = { onNavigateToQuestGuide("Elizabeth") }
                        )
                    }
                }
                "p4", "p4g" -> {
                    item {
                        GuideHubCard(
                            title = "Margaret's Fusion Guides",
                            subtitle = "Step-by-step solutions to raise Empress Arcana Social Link.",
                            icon = Icons.Default.MenuBook,
                            accentColor = accentColor,
                            onClick = { onNavigateToQuestGuide("Margaret") }
                        )
                    }
                    item {
                        GuideHubCard(
                            title = "The Fox's Requests",
                            subtitle = "Complete town wish quests to lower healing costs in dungeons.",
                            icon = Icons.Default.Pets,
                            accentColor = accentColor,
                            onClick = { onNavigateToQuestGuide("The Fox") }
                        )
                    }
                }
                "p5", "p5r" -> {
                    item {
                        GuideHubCard(
                            title = "The Twins' Fusion Training",
                            subtitle = "Unlock powerful execution fusion features with Justine & Caroline.",
                            icon = Icons.Default.MenuBook,
                            accentColor = accentColor,
                            onClick = { onNavigateToQuestGuide("The Twins") }
                        )
                    }
                }
            }

            // Boss Prep Guides (Available for all games)
            item {
                GuideHubCard(
                    title = "Boss Strategy & Builds",
                    subtitle = "Weaknesses, phase behaviors, and optimal builds for main story bosses.",
                    icon = Icons.Default.EmojiEvents,
                    accentColor = TagAlmighty,
                    onClick = onNavigateToBossGuide
                )
            }
        }
    }
}

@Composable
fun GuideHubCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}
