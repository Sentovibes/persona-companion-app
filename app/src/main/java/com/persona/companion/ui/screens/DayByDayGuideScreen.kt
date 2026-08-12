package com.persona.companion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.persona.companion.data.SeriesData
import com.persona.companion.models.DayGuideDay
import com.persona.companion.ui.theme.*
import com.persona.companion.utils.JsonLoader

private data class DayCategoryStyle(val label: String, val color: Color, val icon: ImageVector)

private fun categoryStyle(category: String, accent: Color): DayCategoryStyle =
    when (category.lowercase()) {
        "deadline" -> DayCategoryStyle("Deadline", AccentRed, Icons.Default.Warning)
        "exam"     -> DayCategoryStyle("Exams", TagElec, Icons.Default.School)
        "unlock"   -> DayCategoryStyle("Unlock", AccentGreen, Icons.Default.LockOpen)
        "event"    -> DayCategoryStyle("Event", TagPsychic, Icons.Default.CalendarMonth)
        "tip"      -> DayCategoryStyle("Tip", AccentBrass, Icons.Default.Lightbulb)
        "free"     -> DayCategoryStyle("Free Day", TagPhys, Icons.Default.WbSunny)
        else       -> DayCategoryStyle("Story", accent, Icons.AutoMirrored.Filled.MenuBook)
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayByDayGuideScreen(
    seriesId: String,
    gameId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val series = remember(seriesId) { SeriesData.findSeries(seriesId) }
    val accentColor = series?.color ?: Persona3Blue

    val months = remember(gameId) {
        JsonLoader.loadDayGuides(context, "data/guides/day_guides.json")
            .find { it.gameId.equals(gameId, ignoreCase = true) }
            ?.months ?: emptyList()
    }

    var selectedMonthIndex by remember { mutableIntStateOf(0) }
    val selectedMonth = months.getOrNull(selectedMonthIndex)

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Day-by-Day Guide", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(SeriesData.findGame(seriesId, gameId)?.title ?: "", color = TextSecondary, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        if (months.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No calendar guide available for this game yet.", color = TextSecondary, fontSize = 15.sp)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Month selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                months.forEachIndexed { index, month ->
                    val selected = index == selectedMonthIndex
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) accentColor else SurfaceCard,
                        modifier = Modifier.clickable { selectedMonthIndex = index }
                    ) {
                        Text(
                            text = month.month,
                            color = if (selected) Color.White else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            selectedMonth?.let { month ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Month overview
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${month.month} at a glance",
                                    color = accentColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = month.overview,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    items(month.days) { day ->
                        DayGuideCard(day = day, accentColor = accentColor)
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun DayGuideCard(day: DayGuideDay, accentColor: Color) {
    val style = categoryStyle(day.category, accentColor)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            // Date badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = style.color.copy(alpha = 0.15f),
                modifier = Modifier.width(56.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = style.icon,
                        contentDescription = null,
                        tint = style.color,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = day.date,
                        color = style.color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = day.title,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = style.color.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = style.label.uppercase(),
                            color = style.color,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = day.description,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
