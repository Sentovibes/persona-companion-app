package com.persona.companion.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.persona.companion.R
import com.persona.companion.cast.CastManager
import com.persona.companion.data.UserPreferences
import com.persona.companion.models.Enemy
import com.persona.companion.ui.components.AdaptiveDetailLayout
import com.persona.companion.ui.theme.*
import com.persona.companion.utils.DeviceType
import com.persona.companion.utils.FilterUtils
import com.persona.companion.utils.rememberContentPadding
import com.persona.companion.utils.rememberDeviceType
import com.persona.companion.utils.rememberShouldLoadImages
import com.persona.companion.utils.rememberTextScaleFactor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnemyDetailScreen(
    enemy: Enemy,
    gameId: String = "",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val enemyId = FilterUtils.getEnemyId(enemy)
    var isFavorite by remember { mutableStateOf(userPrefs.isFavoriteEnemy(enemyId)) }
    val shouldLoadImages = rememberShouldLoadImages()
    val deviceType = rememberDeviceType()
    val textScale = rememberTextScaleFactor()
    
    // Broadcast to cast if connected
    LaunchedEffect(enemy) {
        if (CastManager.isServerRunning()) {
            CastManager.broadcastEnemy(enemy)
        }
    }
    
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        enemy.name, 
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = (MaterialTheme.typography.titleLarge.fontSize * textScale)
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isFavorite) {
                            userPrefs.removeFavoriteEnemy(enemyId)
                        } else {
                            userPrefs.addFavoriteEnemy(enemyId)
                        }
                        isFavorite = !isFavorite
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color(0xFFE91E63) else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        AdaptiveDetailLayout(
            modifier = Modifier.padding(padding),
            statsContent = {
                EnemyStatsContent(enemy = enemy, gameId = gameId, textScale = textScale)
            },
            imageContent = {
                if (shouldLoadImages) {
                    EnemyImagePlaceholder(enemy = enemy, deviceType = deviceType)
                }
            }
        )
    }
}

@Composable
private fun EnemyStatsContent(
    enemy: Enemy,
    gameId: String,
    textScale: Float
) {
    // Basic Info
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
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = (MaterialTheme.typography.titleMedium.fontSize * textScale)
                        ),
                        color = TextSecondary
                    )
                    Text(
                        text = "Level ${enemy.level}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = (MaterialTheme.typography.bodyLarge.fontSize * textScale)
                        ),
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${enemy.hp} HP",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = (MaterialTheme.typography.bodyLarge.fontSize * textScale)
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "${enemy.sp} SP",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (MaterialTheme.typography.bodyMedium.fontSize * textScale)
                        ),
                        color = TextSecondary
                    )
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Version (for P5 bosses)
    if (!enemy.version.isNullOrEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Text(
                text = enemy.version,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (MaterialTheme.typography.bodyMedium.fontSize * textScale)
                ),
                color = TextSecondary,
                modifier = Modifier.padding(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    // Stats (if available)
    if (enemy.stats != null) {
        SectionCard(title = "Stats", textScale = textScale) {
            StatRow("Strength", enemy.stats.strength, textScale)
            StatRow("Magic", enemy.stats.magic, textScale)
            StatRow("Endurance", enemy.stats.endurance, textScale)
            StatRow("Agility", enemy.stats.agility, textScale)
            StatRow("Luck", enemy.stats.luck, textScale)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    // Resistances
    SectionCard(title = "Resistances", textScale = textScale) {
        Text(
            text = parseResistances(enemy.resists, gameId),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = (MaterialTheme.typography.bodyMedium.fontSize * textScale)
            ),
            color = TextPrimary
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Skills
    if (enemy.skills.isNotEmpty()) {
        SectionCard(title = "Skills", textScale = textScale) {
            enemy.skills.forEach { skill ->
                Text(
                    text = "• $skill",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (MaterialTheme.typography.bodyMedium.fontSize * textScale)
                    ),
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    // Phases (for multi-phase bosses)
    if (!enemy.phases.isNullOrEmpty()) {
        enemy.phases.forEach { phase ->
            SectionCard(title = "Phase: ${phase.name}", textScale = textScale) {
                InfoRow("HP", phase.hp.toString(), textScale)
                InfoRow("SP", phase.sp.toString(), textScale)
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Resistances",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (MaterialTheme.typography.bodyMedium.fontSize * textScale)
                    ),
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = parseResistances(phase.resists, gameId),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (MaterialTheme.typography.bodySmall.fontSize * textScale)
                    ),
                    color = TextPrimary
                )
                
                if (phase.skills.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Skills",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (MaterialTheme.typography.bodyMedium.fontSize * textScale)
                        ),
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    phase.skills.forEach { skill ->
                        Text(
                            text = "• $skill",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = (MaterialTheme.typography.bodySmall.fontSize * textScale)
                            ),
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
                
                // Parts within phase
                if (!phase.parts.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Parts",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (MaterialTheme.typography.bodyMedium.fontSize * textScale)
                        ),
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
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = (MaterialTheme.typography.bodyMedium.fontSize * textScale)
                                ),
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "HP: ${part.hp}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = (MaterialTheme.typography.bodySmall.fontSize * textScale)
                                ),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Parts (for bosses with parts but no phases)
    if (!enemy.parts.isNullOrEmpty() && enemy.phases.isNullOrEmpty()) {
        enemy.parts.forEach { part ->
            SectionCard(title = "Part: ${part.name}", textScale = textScale) {
                InfoRow("HP", part.hp.toString(), textScale)
                if (part.sp != null) {
                    InfoRow("SP", part.sp.toString(), textScale)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Resistances",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (MaterialTheme.typography.bodyMedium.fontSize * textScale)
                    ),
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = parseResistances(part.resists, gameId),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (MaterialTheme.typography.bodySmall.fontSize * textScale)
                    ),
                    color = TextPrimary
                )
                
                if (!part.skills.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Skills",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (MaterialTheme.typography.bodyMedium.fontSize * textScale)
                        ),
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    part.skills.forEach { skill ->
                        Text(
                            text = "• $skill",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = (MaterialTheme.typography.bodySmall.fontSize * textScale)
                            ),
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Location & Drops
    if (enemy.drops != null) {
        SectionCard(title = "Location & Drops", textScale = textScale) {
            if (enemy.area.isNotEmpty() && enemy.area != "Unknown") {
                InfoRow("Area", enemy.area, textScale)
            }
            if (enemy.exp > 0) {
                InfoRow("EXP", enemy.exp.toString(), textScale)
            }
            if (enemy.drops.gem != "-") {
                InfoRow("Gem Drop", enemy.drops.gem, textScale)
            }
            if (enemy.drops.item != "-") {
                InfoRow("Item Drop", enemy.drops.item, textScale)
            }
        }
    }
}

@Composable
private fun EnemyImagePlaceholder(enemy: Enemy, deviceType: DeviceType) {
    // Placeholder for enemy image
    // In a real implementation, you would load actual enemy images here
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = enemy.name,
                style = when (deviceType) {
                    DeviceType.TV, DeviceType.CAST -> MaterialTheme.typography.headlineLarge
                    else -> MaterialTheme.typography.titleLarge
                },
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Image Placeholder",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDisabled
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "(Add enemy images to assets)",
                style = MaterialTheme.typography.bodySmall,
                color = TextDisabled
            )
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    textScale: Float = 1.0f,
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
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = (MaterialTheme.typography.titleMedium.fontSize * textScale)
            ),
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
fun StatRow(label: String, value: Int, textScale: Float = 1.0f) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = (MaterialTheme.typography.bodyMedium.fontSize * textScale)
            ),
            color = TextSecondary
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = (MaterialTheme.typography.bodyMedium.fontSize * textScale)
            ),
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
fun InfoRow(label: String, value: String, textScale: Float = 1.0f) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = (MaterialTheme.typography.bodyMedium.fontSize * textScale)
            ),
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = (MaterialTheme.typography.bodyMedium.fontSize * textScale)
            ),
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
