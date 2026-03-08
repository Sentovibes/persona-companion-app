package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.persona.companion.models.Enemy
import com.persona.companion.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnemyDetailScreen(
    enemy: Enemy,
    gameId: String = "",
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text(enemy.name, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = enemy.arcana,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Level ${enemy.level}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${enemy.hp} HP",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${enemy.sp} SP",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
            
            // Version (for P5 bosses)
            if (!enemy.version.isNullOrEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                    ) {
                        Text(
                            text = enemy.version,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            
            // Stats (if available)
            if (enemy.stats != null) {
                item {
                    SectionCard(title = "Stats") {
                        StatRow("Strength", enemy.stats.strength)
                        StatRow("Magic", enemy.stats.magic)
                        StatRow("Endurance", enemy.stats.endurance)
                        StatRow("Agility", enemy.stats.agility)
                        StatRow("Luck", enemy.stats.luck)
                    }
                }
            }
            
            // Resistances
            item {
                SectionCard(title = "Resistances") {
                    Text(
                        text = parseResistances(enemy.resists, gameId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
            
            // Skills
            if (enemy.skills.isNotEmpty()) {
                item {
                    SectionCard(title = "Skills") {
                        enemy.skills.forEach { skill ->
                            Text(
                                text = "• $skill",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            // Phases (for multi-phase bosses)
            if (!enemy.phases.isNullOrEmpty()) {
                items(enemy.phases) { phase ->
                    SectionCard(title = "Phase: ${phase.name}") {
                        InfoRow("HP", phase.hp.toString())
                        InfoRow("SP", phase.sp.toString())
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Resistances",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = parseResistances(phase.resists, gameId),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                        
                        if (phase.skills.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Skills",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            phase.skills.forEach { skill ->
                                Text(
                                    text = "• $skill",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                        
                        // Parts within phase (like Yaldabaoth's arms)
                        if (!phase.parts.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Parts",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            phase.parts.forEach { part ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Surface)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = part.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "HP: ${part.hp}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Parts (for bosses with parts but no phases)
            if (!enemy.parts.isNullOrEmpty() && enemy.phases.isNullOrEmpty()) {
                items(enemy.parts) { part ->
                    SectionCard(title = "Part: ${part.name}") {
                        InfoRow("HP", part.hp.toString())
                        if (part.sp != null) {
                            InfoRow("SP", part.sp.toString())
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Resistances",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = parseResistances(part.resists, gameId),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                        
                        if (!part.skills.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Skills",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            part.skills.forEach { skill ->
                                Text(
                                    text = "• $skill",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Location & Drops
            if (enemy.drops != null) {
                item {
                    SectionCard(title = "Location & Drops") {
                        if (enemy.area.isNotEmpty() && enemy.area != "Unknown") {
                            InfoRow("Area", enemy.area)
                        }
                        if (enemy.exp > 0) {
                            InfoRow("EXP", enemy.exp.toString())
                        }
                        if (enemy.drops.gem != "-") {
                            InfoRow("Gem Drop", enemy.drops.gem)
                        }
                        if (enemy.drops.item != "-") {
                            InfoRow("Item Drop", enemy.drops.item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
fun StatRow(label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}

fun parseResistances(resists: String, gameId: String = ""): String {
    // Element names by game
    val elements = when {
        gameId.startsWith("p5") -> listOf("Phys", "Gun", "Fire", "Ice", "Elec", "Wind", "Psy", "Nuke", "Bless", "Curse")
        gameId.startsWith("p4") -> listOf("Phys", "Fire", "Ice", "Elec", "Wind", "Light", "Dark", "Almighty")
        gameId.startsWith("p3") -> listOf("Slash", "Strike", "Pierce", "Fire", "Ice", "Elec", "Wind", "Light", "Dark", "Almighty")
        else -> listOf("Phys", "Fire", "Ice", "Elec", "Wind", "Light", "Dark", "Almighty") // Default to P4
    }
    
    val resistMap = mapOf(
        '-' to "Normal",
        'w' to "Weak",
        's' to "Strong",
        'r' to "Resist",
        'n' to "Null",
        'd' to "Drain"
    )
    
    return resists.mapIndexed { index, char ->
        if (index < elements.size) {
            val resist = resistMap[char] ?: "Normal"
            if (resist != "Normal") {
                "${elements[index]}: $resist"
            } else null
        } else null
    }.filterNotNull().joinToString("\n").ifEmpty { "No special resistances" }
}
