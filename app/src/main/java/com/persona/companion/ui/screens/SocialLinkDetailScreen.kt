package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.persona.companion.models.DialogueChoice
import com.persona.companion.models.SocialLinkRank
import com.persona.companion.ui.theme.*
import com.persona.companion.ui.viewmodels.SocialLinkViewModel
import com.persona.companion.utils.personaImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialLinkDetailScreen(
    gameId: String,
    arcana: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SocialLinkViewModel = viewModel()
    val socialLinksData by viewModel.socialLinksData.collectAsState()
    
    LaunchedEffect(gameId) {
        if (socialLinksData == null) {
            viewModel.loadSocialLinks(gameId)
        }
    }
    
    val socialLink = socialLinksData?.socialLinks?.find { 
        it.arcana.equals(arcana, ignoreCase = true) 
    }
    
    // Get game name for sharing
    val gameName = when (gameId) {
        "p3fes" -> "Persona 3 FES"
        "p3p" -> "Persona 3 Portable"
        "p3r" -> "Persona 3 Reload"
        "p4" -> "Persona 4"
        "p4g" -> "Persona 4 Golden"
        "p5" -> "Persona 5"
        "p5r" -> "Persona 5 Royal"
        else -> "Persona"
    }
    
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text(arcana, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    if (socialLink != null) {
                        IconButton(onClick = {
                            com.persona.companion.utils.ShareUtils.shareSocialLink(
                                context,
                                socialLink,
                                gameName
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        if (socialLink != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(socialLink.ranks) { rank ->
                    RankCard(rank = rank)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Text(
                    text = "Social Link not found",
                    color = TextSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun RankCard(rank: SocialLinkRank) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Rank header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = rank.rankName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                if (rank.isAuto) {
                    Box(
                        modifier = Modifier
                            .background(AccentGreen.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Auto",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Requirements
            if (rank.requirements != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Requirements: ${rank.requirements}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            
            // Next rank points
            if (rank.nextRankPoints > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Points needed: ${rank.nextRankPoints}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentBlue
                )
            }
            
            // Dialogue choices
            if (rank.choices.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = TextSecondary.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Dialogue Choices:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                rank.choices.forEach { choice ->
                    DialogueChoiceItem(choice = choice)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun DialogueChoiceItem(choice: DialogueChoice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (choice.points >= 3) AccentGreen.copy(alpha = 0.1f)
                else if (choice.points > 0) AccentBlue.copy(alpha = 0.1f)
                else Surface.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (choice.isPhoneChoice) {
                Text(
                    text = "📱 Phone",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentBlue
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            
            Text(
                text = choice.text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Box(
            modifier = Modifier
                .background(
                    when {
                        choice.points >= 3 -> AccentGreen
                        choice.points > 0 -> AccentBlue
                        else -> TextSecondary
                    },
                    RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (choice.points > 0) "+${choice.points}" else "${choice.points}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}
