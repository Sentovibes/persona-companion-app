package com.persona.companion.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.persona.companion.data.repositories.SkillRepository
import com.persona.companion.models.Persona
import com.persona.companion.models.PersonaSeries
import com.persona.companion.models.Skill
import com.persona.companion.navigation.Screen
import com.persona.companion.ui.components.AdaptiveDetailLayout
import com.persona.companion.ui.components.AffinityElementMatrix
import com.persona.companion.ui.components.FullImageDialog
import com.persona.companion.ui.components.ProfileImage
import com.persona.companion.ui.components.elementIconRes
import com.persona.companion.ui.components.elementIconTint
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
    var skillsMap by remember { mutableStateOf<Map<String, Skill>>(emptyMap()) }

    LaunchedEffect(personaName, game.dataPath, game.skillPath) {
        persona = withContext(kotlinx.coroutines.Dispatchers.IO) {
            PersonaRepository(context).getPersonaByName(game.dataPath, personaName)
        }
        val path = game.skillPath
        if (!path.isNullOrBlank()) {
            skillsMap = withContext(kotlinx.coroutines.Dispatchers.IO) {
                SkillRepository(context).getSkills(path).associateBy { it.name.lowercase() }
            }
        }
    }

    val personaId = persona?.let { FilterUtils.getPersonaId(seriesId, gameId, it) } ?: ""
    var isFavorite by remember(personaId) { mutableStateOf(personaId.isNotEmpty() && userPrefs.isFavoritePersona(personaId)) }
    var isOwned by remember(personaId) { mutableStateOf(personaId.isNotEmpty() && userPrefs.isPersonaOwned(personaId)) }
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
                        TextButton(
                            onClick = {
                                navController.navigate(Screen.Fusion.createRoute(seriesId, gameId, p.name))
                            }
                        ) {
                            Text(
                                text = "Fuse",
                                color = series.color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = {
                            if (personaId.isNotEmpty()) {
                                isOwned = userPrefs.toggleOwnedPersona(personaId)
                            }
                        }) {
                            Icon(
                                imageVector = if (isOwned) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                                contentDescription = if (isOwned) "Registered in Compendium" else "Mark as Registered",
                                tint = if (isOwned) Color(0xFF4CAF50) else TextSecondary
                            )
                        }
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
                    skillsMap = skillsMap,
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
private fun PersonaStatsContent(
    persona: Persona,
    series: PersonaSeries,
    gameId: String = "",
    skillsMap: Map<String, Skill> = emptyMap(),
    onImageClick: () -> Unit = {}
) {
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

    if (!persona.inherits.isNullOrBlank()) {
        InheritanceSection(persona.inherits)
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (!persona.skills.isNullOrEmpty()) {
        SectionHeader("Skills")
        Spacer(modifier = Modifier.height(8.dp))
        persona.skills.forEach { (skillName, skillLevel) ->
            SkillRow(
                name = skillName,
                level = skillLevel,
                accentColor = series.color,
                skillInfo = skillsMap[skillName.lowercase()]
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    AffinitiesSection(persona, gameId)
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
private fun SkillRow(
    name: String,
    level: Double,
    accentColor: Color,
    skillInfo: Skill? = null
) {
    var expanded by remember(name) { mutableStateOf(false) }
    val label = when {
        level < 1.0 -> "Innate"
        level >= 100 -> "Special"
        else -> "Lv. ${level.toInt()}"
    }
    val labelColor = when {
        level < 1.0 -> accentColor
        level >= 100 -> Color(0xFFFFD700)
        else -> TextSecondary
    }
    val elemIcon = skillInfo?.let { elementIconRes(it.element) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard)
            .let { mod ->
                if (skillInfo != null) mod.clickable { expanded = !expanded } else mod
            }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (elemIcon != null) {
                Icon(
                    painter = painterResource(id = elemIcon),
                    contentDescription = skillInfo.element,
                    tint = elementIconTint(skillInfo.element),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                fontWeight = FontWeight.SemiBold
            )
            if (skillInfo != null) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse skill details" else "Expand skill details",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (skillInfo != null) {
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = Divider.copy(alpha = 0.4f))
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentColor.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = skillInfo.element,
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        if (skillInfo.cost > 0 && skillInfo.costType.isNotBlank()) {
                            val costColor = if (skillInfo.costType == "SP") Color(0xFF00CEC9) else Color(0xFFFF6B6B)
                            Text(
                                text = "${skillInfo.cost} ${skillInfo.costType}",
                                style = MaterialTheme.typography.labelSmall,
                                color = costColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (skillInfo.effect.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = skillInfo.effect,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AffinitiesSection(persona: Persona, gameId: String) {
    val grouped = remember(persona, gameId) {
        buildMap<String, List<String>> {
            if (persona.weaknesses.isNotEmpty()) put("Weak", persona.weaknesses)
            if (persona.resistances.isNotEmpty()) put("Resist", persona.resistances)
            if (persona.nullifies.isNotEmpty()) put("Null", persona.nullifies)
            if (persona.repels.isNotEmpty()) put("Repel", persona.repels)
            if (persona.absorbs.isNotEmpty()) put("Absorb", persona.absorbs)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SectionHeader("Affinities")
        AffinityElementMatrix(gameId = gameId, grouped = grouped)
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

@Composable
private fun InheritanceSection(inheritsType: String) {
    val type = inheritsType.trim().lowercase()
    val restrictions = when (type) {
        "fire" -> listOf("Cannot inherit: Ice skills")
        "ice" -> listOf("Cannot inherit: Fire skills")
        "elec" -> listOf("Cannot inherit: Wind skills")
        "wind" -> listOf("Cannot inherit: Elec skills")
        "bless", "light" -> listOf("Cannot inherit: Curse/Dark, Ailment, Physical skills")
        "curse", "dark" -> listOf("Cannot inherit: Bless/Light, Healing/Recovery skills")
        "phys", "physical" -> listOf("Cannot inherit: Magic attack skills")
        "ailment" -> listOf("Cannot inherit: Bless/Light, Healing skills")
        "recovery", "healing" -> listOf("Cannot inherit: Curse/Dark, Physical attack skills")
        "almighty" -> listOf("Can inherit all skill affinities")
        else -> emptyList()
    }

    SectionHeader("Skill Inheritance")
    Spacer(modifier = Modifier.height(8.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, Hairline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inheritance Affinity",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SurfaceRaised,
                    border = BorderStroke(1.dp, HairlineSoft)
                ) {
                    Text(
                        text = inheritsType.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AccentBrass,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            if (restrictions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                restrictions.forEach { restriction ->
                    Text(
                        text = restriction,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
