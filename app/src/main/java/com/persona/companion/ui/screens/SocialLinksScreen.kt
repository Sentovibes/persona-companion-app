package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.persona.companion.models.SocialLink
import com.persona.companion.ui.theme.*
import com.persona.companion.ui.viewmodels.SocialLinkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialLinksScreen(
    gameId: String,
    gameName: String,
    onBack: () -> Unit,
    onSocialLinkClick: (String) -> Unit
) {
    val viewModel: SocialLinkViewModel = viewModel()
    val socialLinksData by viewModel.socialLinksData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }

    val themeColor = when {
        gameId.startsWith("p5") -> Persona5Red
        gameId.startsWith("p4") -> Persona4Yellow
        else -> Persona3Blue
    }

    val isP5 = gameId.startsWith("p5")
    val titlePrefix = if (isP5) "Confidants" else "Social Links"

    // Re-load when gameId changes OR when protagonist preference changes (for P3P)
    val userPrefs = remember { com.persona.companion.data.UserPreferences(context) }
    val noSpoilersMode by userPrefs.noSpoilersModeFlow.collectAsState(initial = userPrefs.isNoSpoilersMode())
    val pinnedLinks by userPrefs.pinnedSocialLinksFlow.collectAsState(initial = userPrefs.getPinnedSocialLinks())
    val maxedLinks by userPrefs.maxedSocialLinksFlow.collectAsState(initial = userPrefs.getMaxedSocialLinks())
    val revealedSpoilers = remember { mutableStateMapOf<String, Boolean>() }

    val protagonist = if (gameId == "p3p") {
        remember { userPrefs.getP3PProtagonist().name }
    } else {
        ""
    }

    LaunchedEffect(gameId, protagonist) {
        viewModel.loadSocialLinks(gameId)
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = titlePrefix,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = gameName,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = {
                    Text(
                        if (isP5) "Search confidant or arcana..." else "Search character or arcana...",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedBorderColor = themeColor,
                    unfocusedBorderColor = HairlineStrong,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = themeColor
                        )
                    }
                    error != null -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = error ?: "Unknown error",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    socialLinksData != null -> {
                        val allLinks = socialLinksData!!.socialLinks
                        val filteredLinks = remember(allLinks, searchQuery) {
                            if (searchQuery.isBlank()) {
                                allLinks
                            } else {
                                val q = searchQuery.trim().lowercase()
                                allLinks.filter { link ->
                                    link.arcana.lowercase().contains(q) ||
                                    (link.characterName?.lowercase()?.contains(q) == true) ||
                                    (link.details?.location?.lowercase()?.contains(q) == true)
                                }
                            }
                        }

                        if (filteredLinks.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No matching ${if (isP5) "confidants" else "social links"} found",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            SocialLinksList(
                                gameId = gameId,
                                isP5 = isP5,
                                socialLinks = filteredLinks,
                                themeColor = themeColor,
                                noSpoilersMode = noSpoilersMode,
                                searchActive = searchQuery.isNotBlank(),
                                pinnedLinks = pinnedLinks,
                                maxedLinks = maxedLinks,
                                getPrefKey = { arcana -> userPrefs.getSocialLinkKey(gameId, arcana) },
                                onTogglePin = { arcana ->
                                    userPrefs.togglePinSocialLink(userPrefs.getSocialLinkKey(gameId, arcana))
                                },
                                onToggleMax = { arcana ->
                                    userPrefs.toggleMaxedSocialLink(userPrefs.getSocialLinkKey(gameId, arcana))
                                },
                                revealedSpoilers = revealedSpoilers,
                                onReveal = { arcana -> revealedSpoilers[arcana] = true },
                                onSocialLinkClick = onSocialLinkClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SocialLinksList(
    gameId: String,
    isP5: Boolean,
    socialLinks: List<SocialLink>,
    themeColor: Color,
    noSpoilersMode: Boolean,
    searchActive: Boolean,
    pinnedLinks: Set<String>,
    maxedLinks: Set<String>,
    getPrefKey: (String) -> String,
    onTogglePin: (String) -> Unit,
    onToggleMax: (String) -> Unit,
    revealedSpoilers: Map<String, Boolean>,
    onReveal: (String) -> Unit,
    onSocialLinkClick: (String) -> Unit
) {
    var showMaxedSection by remember { mutableStateOf(true) }

    val pinnedList = remember(socialLinks, pinnedLinks, gameId) {
        socialLinks.filter { pinnedLinks.contains(getPrefKey(it.arcana)) }
    }
    val maxedList = remember(socialLinks, pinnedLinks, maxedLinks, gameId) {
        socialLinks.filter {
            val key = getPrefKey(it.arcana)
            !pinnedLinks.contains(key) && maxedLinks.contains(key)
        }
    }
    val activeList = remember(socialLinks, pinnedLinks, maxedLinks, gameId) {
        socialLinks.filter {
            val key = getPrefKey(it.arcana)
            !pinnedLinks.contains(key) && !maxedLinks.contains(key)
        }
    }

    val hasGroups = pinnedList.isNotEmpty() || maxedList.isNotEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (pinnedList.isNotEmpty()) {
            item(key = "header_pinned") {
                SocialLinkSectionHeader(
                    title = "PINNED",
                    count = pinnedList.size,
                    accentColor = AccentBrass
                )
            }
            items(pinnedList, key = { "pinned_${it.arcana}" }) { socialLink ->
                val prefKey = getPrefKey(socialLink.arcana)
                val isSpoiler = com.persona.companion.utils.SpoilerUtils.isSpoilerSocialLink(
                    socialLink.arcana,
                    socialLink.characterName
                )
                val isRevealed = revealedSpoilers[socialLink.arcana] == true
                val isConcealed = noSpoilersMode && isSpoiler && !isRevealed

                SocialLinkCard(
                    socialLink = socialLink,
                    themeColor = themeColor,
                    isPinned = true,
                    isMaxed = maxedLinks.contains(prefKey),
                    isConcealed = isConcealed,
                    onTogglePin = { onTogglePin(socialLink.arcana) },
                    onToggleMax = { onToggleMax(socialLink.arcana) },
                    onReveal = { onReveal(socialLink.arcana) },
                    onClick = { onSocialLinkClick(socialLink.arcana) }
                )
            }
        }

        if (activeList.isNotEmpty()) {
            if (hasGroups) {
                item(key = "header_active") {
                    SocialLinkSectionHeader(
                        title = if (isP5) "CONFIDANTS" else "SOCIAL LINKS",
                        count = activeList.size,
                        accentColor = themeColor
                    )
                }
            }
            items(activeList, key = { "active_${it.arcana}" }) { socialLink ->
                val isSpoiler = com.persona.companion.utils.SpoilerUtils.isSpoilerSocialLink(
                    socialLink.arcana,
                    socialLink.characterName
                )
                val isRevealed = revealedSpoilers[socialLink.arcana] == true
                val isConcealed = noSpoilersMode && isSpoiler && !isRevealed

                SocialLinkCard(
                    socialLink = socialLink,
                    themeColor = themeColor,
                    isPinned = false,
                    isMaxed = false,
                    isConcealed = isConcealed,
                    onTogglePin = { onTogglePin(socialLink.arcana) },
                    onToggleMax = { onToggleMax(socialLink.arcana) },
                    onReveal = { onReveal(socialLink.arcana) },
                    onClick = { onSocialLinkClick(socialLink.arcana) }
                )
            }
        }

        if (maxedList.isNotEmpty()) {
            item(key = "header_maxed") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showMaxedSection = !showMaxedSection }
                        .padding(top = 8.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 3.dp, height = 14.dp)
                                .background(AccentGreen, RoundedCornerShape(2.dp))
                        )
                        Text(
                            text = "MAXED (${maxedList.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = AccentGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }
                    Icon(
                        imageVector = if (showMaxedSection || searchActive) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (showMaxedSection) "Collapse maxed" else "Expand maxed",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (showMaxedSection || searchActive) {
                items(maxedList, key = { "maxed_${it.arcana}" }) { socialLink ->
                    val isSpoiler = com.persona.companion.utils.SpoilerUtils.isSpoilerSocialLink(
                        socialLink.arcana,
                        socialLink.characterName
                    )
                    val isRevealed = revealedSpoilers[socialLink.arcana] == true
                    val isConcealed = noSpoilersMode && isSpoiler && !isRevealed

                    SocialLinkCard(
                        socialLink = socialLink,
                        themeColor = themeColor,
                        isPinned = false,
                        isMaxed = true,
                        isConcealed = isConcealed,
                        onTogglePin = { onTogglePin(socialLink.arcana) },
                        onToggleMax = { onToggleMax(socialLink.arcana) },
                        onReveal = { onReveal(socialLink.arcana) },
                        onClick = { onSocialLinkClick(socialLink.arcana) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SocialLinkSectionHeader(
    title: String,
    count: Int,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 14.dp)
                .background(accentColor, RoundedCornerShape(2.dp))
        )
        Text(
            text = "$title ($count)",
            style = MaterialTheme.typography.labelLarge,
            color = accentColor,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
private fun SocialLinkCard(
    socialLink: SocialLink,
    themeColor: Color,
    isPinned: Boolean = false,
    isMaxed: Boolean = false,
    isConcealed: Boolean = false,
    onTogglePin: () -> Unit = {},
    onToggleMax: () -> Unit = {},
    onReveal: () -> Unit = {},
    onClick: () -> Unit
) {
    val charName = socialLink.characterName
    val hasCharName = !charName.isNullOrBlank()

    if (isConcealed) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onReveal),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Concealed Confidant (Spoiler)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE57373),
                            fontSize = 15.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE57373).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFFE57373).copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SPOILER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE57373)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Plot-critical confidant hidden · Tap to reveal",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDisabled,
                        fontSize = 12.sp
                    )
                }

                TextButton(onClick = onReveal) {
                    Text("Reveal", color = Color(0xFFE57373), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val borderColor = when {
        isPinned -> AccentBrass.copy(alpha = 0.5f)
        isMaxed -> AccentGreen.copy(alpha = 0.25f)
        else -> HairlineSoft
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isMaxed && !isPinned) 0.72f else 1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (hasCharName) charName!! else socialLink.arcana,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )

                    if (hasCharName) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(themeColor.copy(alpha = 0.15f))
                                .border(1.dp, themeColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = socialLink.arcana.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColor
                            )
                        }
                    }

                    if (isMaxed) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentGreen.copy(alpha = 0.15f))
                                .border(1.dp, AccentGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "MAX",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGreen
                            )
                        }
                    }
                }

                val subtitleText = socialLink.details?.location ?: socialLink.details?.schedule
                if (!subtitleText.isNullOrBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (socialLink.details?.location != null) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = TextDisabled,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Pin, Max, and Chevron controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (isPinned) "Unpin" else "Pin to top",
                        tint = if (isPinned) AccentBrass else TextDisabled,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onToggleMax,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isMaxed) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = if (isMaxed) "Unmark maxed" else "Mark maxed",
                        tint = if (isMaxed) AccentGreen else TextDisabled,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View",
                    tint = TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}