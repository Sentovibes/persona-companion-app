package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                    Text(
                        if (gameId.startsWith("p5")) "Confidants - $gameName" else "Social Links - $gameName",
                        color = TextPrimary
                    ) 
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AccentBlue
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
                    SocialLinksList(
                        socialLinks = socialLinksData!!.socialLinks,
                        onSocialLinkClick = onSocialLinkClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SocialLinksList(
    socialLinks: List<SocialLink>,
    onSocialLinkClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(socialLinks) { socialLink ->
            SocialLinkCard(
                socialLink = socialLink,
                onClick = { onSocialLinkClick(socialLink.arcana) }
            )
        }
    }
}

@Composable
private fun SocialLinkCard(
    socialLink: SocialLink,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = socialLink.arcana,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${socialLink.ranks.size} ranks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            
            // Show rank count badge
            Box(
                modifier = Modifier
                    .background(AccentBlue.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Rank ${socialLink.ranks.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
