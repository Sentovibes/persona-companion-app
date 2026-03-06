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
import com.persona.companion.models.SkillEntry
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
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Persona not found.", color = TextSecondary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero header
            item { HeroSection(persona, series.color) }

            // Description
            if (persona.description.isNotBlank()) {
                item { DescriptionSection(persona.description) }
            }

            // Stats
            item { StatsSection(persona, series.color) }

            // Skills
            if (persona.skills.isNotEmpty()) {
                item { SectionHeader("Skills") }
                items(persona.skills) { skill ->
                    SkillRow(skill, series.color)
                }
            }

            // Affinities
            if (persona.weaknesses.isNotEmpty() || persona.resistances.isNotEmpty() ||
                persona.nullifies.isNotEmpty() || persona.repels.isNotEmpty() ||
                persona.absorbs.isNotEmpty()
            ) {
                item { AffinitiesSection(persona) }
            }
        }
    }
}

// ── Sub-sections ─────────────────────────────────────────────────────────────

@Composable
private fun HeroSection(persona: Persona, accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Big level badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = accentColor.copy(alpha = 0.2f),
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text  = "Lv.",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                    Text(
                        text  = "${persona.level}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                text  = persona.name,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Text(
                text  = "${persona.arcana} Arcana",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            if (persona.specialFusion) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = accentColor.copy(alpha = 0.25f)
                ) {
                    Text(
                        text  = "Special Fusion",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DescriptionSection(description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .padding(16.dp)
    ) {
        Text(
            text  = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun StatsSection(persona: Persona, accentColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .padding(16.dp)
    ) {
        SectionHeader("Base Stats")
        Spacer(Modifier.height(12.dp))

        val stats = listOf(
            "STR" to persona.stats.strength,
            "MAG" to persona.stats.magic,
            "END" to persona.stats.endurance,
            "AGI" to persona.stats.agility,
            "LUK" to persona.stats.luck
        )
        val maxStat = stats.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

        stats.forEach { (label, value) ->
            StatBar(label, value, maxStat, accentColor)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StatBar(label: String, value: Int, maxValue: Int, accentColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelLarge,
            color    = TextSecondary,
            modifier = Modifier.width(36.dp)
        )
        Spacer(Modifier.width(8.dp))
        LinearProgressIndicator(
            progress       = { value.toFloat() / maxValue.toFloat() },
            modifier       = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color          = accentColor,
            trackColor     = SurfaceRaised
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text  = "$value",
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp)
        )
    }
}

@Composable
private fun SkillRow(skill: SkillEntry, accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = skill.name,
            style    = MaterialTheme.typography.bodyMedium,
            color    = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        val levelLabel = if (skill.level == 0) "Innate" else "Lv. ${skill.level}"
        Text(
            text  = levelLabel,
            style = MaterialTheme.typography.labelSmall,
            color = if (skill.level == 0) accentColor else TextSecondary
        )
    }
}

@Composable
private fun AffinitiesSection(persona: Persona) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SectionHeader("Affinities")
        Spacer(Modifier.height(4.dp))
        AffinityRow("Weak",    persona.weaknesses,  Color(0xFFE57373))
        AffinityRow("Resists", persona.resistances, Color(0xFF81C784))
        AffinityRow("Null",    persona.nullifies,   Color(0xFFB0BEC5))
        AffinityRow("Repel",   persona.repels,      Color(0xFF64B5F6))
        AffinityRow("Absorb",  persona.absorbs,     Color(0xFFFFD54F))
    }
}

@Composable
private fun AffinityRow(label: String, elements: List<String>, chipColor: Color) {
    if (elements.isEmpty()) return
    Column {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            elements.forEach { element ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = chipColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text     = element,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = chipColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text  = title,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        fontWeight = FontWeight.Bold
    )
}
