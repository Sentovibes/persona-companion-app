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
    onNavigateToDayGuide: () -> Unit,
    onNavigateToNegotiationGuide: () -> Unit = {}
) {
    val series = remember(seriesId) { SeriesData.findSeries(seriesId) }
    val game = remember(gameId) { SeriesData.findGame(seriesId, gameId) }
    val accentColor = series?.color ?: Persona3Blue

    val context = androidx.compose.ui.platform.LocalContext.current
    val userPrefs = remember { com.persona.companion.data.UserPreferences(context) }
    val isFeMC = remember { userPrefs.getP3PProtagonist() == com.persona.companion.data.UserPreferences.P3PProtagonist.FEMC }

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
                "p3p" -> {
                    item {
                        GuideHubCard(
                            title = if (isFeMC) "Theodore's Requests" else "Elizabeth's Requests",
                            subtitle = if (isFeMC)
                                "Fusion walkthroughs, date events, item hunt locations, and combat challenges for Theodore."
                            else
                                "Fusion walkthroughs, date events, item hunt locations, and combat challenges for Elizabeth.",
                            icon = Icons.Default.MenuBook,
                            accentColor = accentColor,
                            onClick = { onNavigateToQuestGuide(if (isFeMC) "Theodore" else "Elizabeth") }
                        )
                    }
                }
                "p3fes", "p3r" -> {
                    item {
                        GuideHubCard(
                            title = "Elizabeth's Requests",
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
                    item {
                        GuideHubCard(
                            title = "Inaba Side-Quests Guide",
                            subtitle = "Comprehensive walkthroughs and answers for all Inaba town resident requests.",
                            icon = Icons.Default.Assignment,
                            accentColor = TagIce,
                            onClick = { onNavigateToQuestGuide("Inaba Residents") }
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
                    item {
                        GuideHubCard(
                            title = "Mementos Target Requests",
                            subtitle = "Step-by-step solutions, target locations, intel triggers, and boss strategies.",
                            icon = Icons.Default.Assignment,
                            accentColor = TagFire,
                            onClick = { onNavigateToQuestGuide("Mementos Targets") }
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

            // Shadow Negotiation / Shuffle Time Guide
            item {
                val negoTitle = when (seriesId) {
                    "p5" -> "Shadow Negotiation Guide"
                    "p3" -> "Shuffle Time & Major Arcana"
                    else -> "Shuffle Time & Sweep Bonus"
                }
                val negoDesc = when (seriesId) {
                    "p5" -> "Upbeat/Timid/Gloomy/Irritable cheat sheet, Sun Confidant perks, and Shadow database."
                    "p3" -> "Major Arcana Tarot effects, Arcana Burst mechanics, and Minor Arcana cards."
                    else -> "Sweep Bonus rules & rewards, Major Arcana tarot, and draw tactics."
                }
                GuideHubCard(
                    title = negoTitle,
                    subtitle = negoDesc,
                    icon = Icons.Default.AutoAwesome,
                    accentColor = accentColor,
                    onClick = onNavigateToNegotiationGuide
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
