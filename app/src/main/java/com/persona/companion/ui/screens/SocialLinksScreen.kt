package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
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
    val protagonist = if (gameId == "p3p") {
        remember { com.persona.companion.data.UserPreferences(context).getP3PProtagonist().name }
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
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
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
                                socialLinks = filteredLinks,
                                themeColor = themeColor,
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
    socialLinks: List<SocialLink>,
    themeColor: Color,
    onSocialLinkClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(socialLinks, key = { it.arcana }) { socialLink ->
            SocialLinkCard(
                socialLink = socialLink,
                themeColor = themeColor,
                onClick = { onSocialLinkClick(socialLink.arcana) }
            )
        }
    }
}

@Composable
private fun SocialLinkCard(
    socialLink: SocialLink,
    themeColor: Color,
    onClick: () -> Unit
) {
    val charName = socialLink.characterName
    val hasCharName = !charName.isNullOrBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp),
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
                    // Main Title: Character Name (if available) or Arcana
                    Text(
                        text = if (hasCharName) charName!! else socialLink.arcana,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )

                    // Arcana Chip Badge
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
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle Row: Ranks count and location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${socialLink.ranks.size} ranks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    socialLink.details?.location?.let { loc ->
                        Text("•", color = TextDisabled, fontSize = 12.sp)
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextDisabled,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = loc,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Rank Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(themeColor.copy(alpha = 0.18f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Rank ${socialLink.ranks.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
