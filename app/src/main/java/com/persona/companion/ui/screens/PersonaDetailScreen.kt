package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.persona.companion.data.PersonaRepository
import com.persona.companion.data.SeriesData
import com.persona.companion.models.Persona
import com.persona.companion.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaDetailScreen(
    navController: NavController,
    seriesId: String,
    gameId: String,
    personaName: String
) {
    val context = LocalContext.current
    val series  = SeriesData.findSeries(seriesId) ?: return
    val game    = SeriesData.findGame(seriesId, gameId) ?: return

    val persona = remember(personaName, game.dataPath) {
        PersonaRepository(context).getPersonaByName(game.dataPath, personaName)
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text(persona?.name ?: "Persona", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        if (persona == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Persona not found.", color = TextSecondary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { HeroSection(persona, series.color) }
            
            if (!persona.description.isNullOrBlank()) {
                item { DescriptionSection(persona.description) }
            }

            item { StatsSection(persona, series.color) }

            if (!persona.skills.isNullOrEmpty()) {
                item { SectionHeader("Skills") }
                items(persona.skills.toList()) { skillEntry ->
                    SkillRow(skillEntry.first, skillEntry.second, series.color)
                }
            }

            item { AffinitiesSection(persona) }
        }
    }
}

@Composable
private fun HeroSection(persona: Persona, accentColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceCard).padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = RoundedCornerShape(12.dp), color = accentColor.copy(alpha = 0.2f), modifier = Modifier.size(64.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Lv.", style = MaterialTheme.typography.labelSmall, color = accentColor)
                    Text(text = "${persona.level}", style = MaterialTheme.typography.headlineMedium, color = accentColor, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = persona.name, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
            Text(text = "${persona.arcana ?: "Unknown"} Arcana", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            if (persona.trait != null) {
                Text(text = "Trait: ${persona.trait}", style = MaterialTheme.typography.labelSmall, color = accentColor)
            }
        }
    }
}

@Composable
private fun DescriptionSection(description: String) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceCard).padding(16.dp)) {
        Text(text = description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
private fun StatsSection(persona: Persona, accentColor: Color) {
    val statsList = persona.stats
    if (statsList == null || statsList.size < 5) return
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceCard).padding(16.dp)) {
        SectionHeader("Base Stats")
        Spacer(Modifier.height(12.dp))
        val labels = listOf("STR", "MAG", "END", "AGI", "LUK")
        val maxStat = statsList.maxOrNull()?.coerceAtLeast(1) ?: 1
        labels.forEachIndexed { i, label ->
            StatBar(label, statsList[i], maxStat, accentColor)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StatBar(label: String, value: Int, maxValue: Int, accentColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = TextSecondary, modifier = Modifier.width(36.dp))
        LinearProgressIndicator(progress = { value.toFloat() / maxValue.toFloat() }, modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)), color = accentColor, trackColor = SurfaceRaised)
        Text(text = "$value", style = MaterialTheme.typography.labelLarge, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp).width(24.dp))
    }
}

@Composable
private fun SkillRow(name: String, level: Double, accentColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SurfaceCard).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
        val label = if (level < 1.0) "Innate" else "Lv. ${level.toInt()}"
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = if (level < 1.0) accentColor else TextSecondary)
    }
}

@Composable
private fun AffinitiesSection(persona: Persona) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceCard).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader("Affinities")
        AffinityRow("Weak", persona.weaknesses, Color(0xFFE57373))
        AffinityRow("Resists", persona.resistances, Color(0xFF81C784))
        AffinityRow("Null", persona.nullifies, Color(0xFFB0BEC5))
        AffinityRow("Repel", persona.repels, Color(0xFF64B5F6))
        AffinityRow("Absorb", persona.absorbs, Color(0xFFFFD54F))
    }
}

@Composable
private fun AffinityRow(label: String, elements: List<String>, chipColor: Color) {
    if (elements.isEmpty()) return
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
            elements.forEach { element ->
                Surface(shape = RoundedCornerShape(6.dp), color = chipColor.copy(alpha = 0.2f)) {
                    Text(text = element, style = MaterialTheme.typography.labelSmall, color = chipColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
}
