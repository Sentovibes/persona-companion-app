package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.persona.companion.R
import com.persona.companion.data.PersonaRepository
import com.persona.companion.data.SeriesData
import com.persona.companion.data.UserPreferences
import com.persona.companion.models.Persona
import com.persona.companion.models.PersonaSeries
import com.persona.companion.ui.components.AdaptiveDetailLayout
import com.persona.companion.ui.components.FullImageDialog
import com.persona.companion.ui.components.ProfileImage
import com.persona.companion.ui.theme.*
import com.persona.companion.utils.DeviceType
import com.persona.companion.utils.FilterUtils
import com.persona.companion.utils.personaImage
import com.persona.companion.utils.rememberDeviceType
import com.persona.companion.utils.rememberShouldLoadImages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    var persona by remember { mutableStateOf<com.persona.companion.models.Persona?>(null) }

    LaunchedEffect(personaName, game.dataPath) {
        persona = withContext(kotlinx.coroutines.Dispatchers.IO) {
            PersonaRepository(context).getPersonaByName(game.dataPath, personaName)
        }
    }

    val personaId = persona?.let { FilterUtils.getPersonaId(seriesId, gameId, it) } ?: ""
    var isFavorite by remember { mutableStateOf(userPrefs.isFavoritePersona(personaId)) }
    var showFullImage by remember { mutableStateOf(false) }
    
    // Track view in history
    LaunchedEffect(persona) {
        persona?.let {
            userPrefs.addRecentPersona(seriesId, gameId, it.name)
        }
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
                actions = {
                    val p = persona
                    if (p != null) {
                        IconButton(onClick = {
                            com.persona.companion.utils.ShareUtils.sharePersona(
                                context,
                                p,
                                game.title
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = TextSecondary
                            )
                        }
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
        val p = persona
        if (p == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        AdaptiveDetailLayout(
            modifier = Modifier.padding(padding),
            statsContent = {
                PersonaStatsContent(
                    persona = p,
                    series = series,
                    gameId = gameId,
                    onImageClick = { showFullImage = true }
                )
            },
            imageContent = {
                if (shouldLoadImages) {
                    PersonaImageDisplay(persona = p, deviceType = deviceType, gameId = gameId)
                }
            }
        )

        // Show full-size image dialog when clicked
        if (showFullImage) {
            FullImageDialog(
                name = p.name,
                isEnemy = false,
                gameId = gameId,
                onDismiss = { showFullImage = false }
            )
        }
    }
}

@Composable
private fun PersonaStatsContent(persona: Persona, series: PersonaSeries, gameId: String = "", onImageClick: () -> Unit = {}) {
    HeroSection(persona, series.color, gameId, onImageClick)
    
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
private fun PersonaImageDisplay(persona: Persona, deviceType: DeviceType, gameId: String) {
    val context = LocalContext.current

    val model = remember(persona.name, gameId) {
        ImageRequest.Builder(context)
            .personaImage(context, gameId, persona.name)
            .crossfade(true)
            .build()
    }

    when (deviceType) {
        DeviceType.PHONE -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                ProfileImage(name = persona.name, isEnemy = false, size = 64, gameId = gameId)
            }
        }
        DeviceType.TABLET, DeviceType.TV, DeviceType.CAST -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = model,
                    contentDescription = persona.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    error = androidx.compose.ui.res.painterResource(R.drawable.placeholder_persona)
                )
            }
        }
    }
}

@Composable
private fun HeroSection(persona: Persona, accentColor: Color, gameId: String = "", onImageClick: () -> Unit = {}) {
    val deviceType = rememberDeviceType()
    val shouldLoadImages = rememberShouldLoadImages()
    
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceCard).padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Show profile image on phone only
        if (deviceType == DeviceType.PHONE && shouldLoadImages) {
            ProfileImage(
                name = persona.name,
                isEnemy = false,
                size = 72,
                gameId = gameId,
                onClick = onImageClick
            )
            Spacer(Modifier.width(16.dp))
        }
        
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AffinityRow(label: String, elements: List<String>, chipColor: Color) {
    if (elements.isEmpty()) return

    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        FlowRow(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            elements.forEach { element ->
                val iconRes = when (element) {
                    "Fire" -> com.persona.companion.R.drawable.ic_fire
                    "Ice" -> com.persona.companion.R.drawable.ic_ice
                    "Elec" -> com.persona.companion.R.drawable.ic_elec
                    "Wind", "Force" -> com.persona.companion.R.drawable.ic_wind
                    "Light" -> com.persona.companion.R.drawable.ic_light
                    "Dark" -> com.persona.companion.R.drawable.ic_dark
                    "Almighty" -> com.persona.companion.R.drawable.ic_almighty
                    "Psy" -> com.persona.companion.R.drawable.ic_psy
                    "Nuclear" -> com.persona.companion.R.drawable.ic_nuke
                    "Phys", "Slash", "Strike", "Pierce" -> com.persona.companion.R.drawable.ic_phys
                    "Recovery" -> com.persona.companion.R.drawable.ic_recovery
                    "Support" -> com.persona.companion.R.drawable.ic_support
                    "Ailment" -> com.persona.companion.R.drawable.ic_ailment
                    else -> null
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = chipColor.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (iconRes != null) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = iconRes),
                                contentDescription = null,
                                tint = chipColor,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = element,
                            style = MaterialTheme.typography.labelSmall,
                            color = chipColor
                        )
                    }
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
