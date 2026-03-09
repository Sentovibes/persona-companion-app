package com.persona.companion.ui.screens

import android.graphics.Bitmap
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.persona.companion.data.PersonaRepository
import com.persona.companion.data.SeriesData
import com.persona.companion.data.UserPreferences
import com.persona.companion.models.Persona
import com.persona.companion.models.PersonaSeries
import com.persona.companion.ui.components.AdaptiveDetailLayout
import com.persona.companion.ui.components.ProfileImage
import com.persona.companion.ui.theme.*
import com.persona.companion.utils.DeviceType
import com.persona.companion.utils.FilterUtils
import com.persona.companion.utils.ImageUtils
import com.persona.companion.utils.rememberDeviceType
import com.persona.companion.utils.rememberShouldLoadImages

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
    val userPrefs = remember { UserPreferences(context) }
    val deviceType = rememberDeviceType()
    val shouldLoadImages = rememberShouldLoadImages()

    val persona = remember(personaName, game.dataPath) {
        PersonaRepository(context).getPersonaByName(game.dataPath, personaName)
    }
    
    val personaId = persona?.let { FilterUtils.getPersonaId(it) } ?: ""
    var isFavorite by remember { mutableStateOf(userPrefs.isFavoritePersona(personaId)) }

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
                actions = {
                    if (persona != null) {
                        IconButton(onClick = {
                            if (isFavorite) {
                                userPrefs.removeFavoritePersona(personaId)
                            } else {
                                userPrefs.addFavoritePersona(personaId)
                            }
                            isFavorite = !isFavorite
                        }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (isFavorite) series.color else TextSecondary
                            )
                        }
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

        AdaptiveDetailLayout(
            modifier = Modifier.padding(padding),
            statsContent = {
                PersonaStatsContent(persona = persona, series = series)
            },
            imageContent = {
                if (shouldLoadImages) {
                    PersonaImageDisplay(persona = persona, deviceType = deviceType)
                }
            }
        )
    }
}

@Composable
private fun PersonaStatsContent(persona: Persona, series: PersonaSeries) {
    HeroSection(persona, series.color)
    
    Spacer(modifier = Modifier.height(16.dp))
    
    if (!persona.description.isNullOrBlank()) {
        DescriptionSection(persona.description)
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    if (!persona.unlock.isNullOrBlank()) {
        UnlockSection(persona.unlock)
        Spacer(modifier = Modifier.height(16.dp))
    }

    StatsSection(persona, series.color)
    
    Spacer(modifier = Modifier.height(16.dp))

    if (!persona.skills.isNullOrEmpty()) {
        SectionHeader("Skills")
        Spacer(modifier = Modifier.height(8.dp))
        persona.skills.forEach { (skillName, skillLevel) ->
            SkillRow(skillName, skillLevel, series.color)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    AffinitiesSection(persona)
}

@Composable
private fun PersonaImageDisplay(persona: Persona, deviceType: DeviceType) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // Load image
    LaunchedEffect(persona.name) {
        val imagePath = ImageUtils.getImagePath(persona.name, isEnemy = false)
        bitmap = ImageUtils.loadImageFromAssets(context, imagePath)
    }
    
    when (deviceType) {
        DeviceType.PHONE -> {
            // Phone: Small circular profile image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                ProfileImage(
                    name = persona.name,
                    isEnemy = false,
                    size = 64
                )
            }
        }
        
        DeviceType.TABLET, DeviceType.TV, DeviceType.CAST -> {
            // Tablet/TV: Full-size image that fits content
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = persona.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Fallback if no image
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
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "No image",
                                tint = TextSecondary.copy(alpha = 0.3f),
                                modifier = Modifier.size(120.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = persona.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
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
        val label = when {
            level < 1.0 -> "Innate"
            level >= 100 -> "Special"
            else -> "Lv. ${level.toInt()}"
        }
        val labelColor = when {
            level < 1.0 -> accentColor
            level >= 100 -> Color(0xFFFFD700) // Gold for special skills
            else -> TextSecondary
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = labelColor)
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


@Composable
private fun UnlockSection(unlock: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A2A3E))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Unlock Requirement",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFFD700)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = unlock,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }
    }
}
