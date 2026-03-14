package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.persona.companion.models.DialogueChoice
import com.persona.companion.models.SocialLinkDetails
import com.persona.companion.models.SocialLinkDialogue
import com.persona.companion.models.SocialLinkRank
import com.persona.companion.ui.theme.*
import com.persona.companion.ui.viewmodels.SocialLinkViewModel

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
        if (socialLinksData == null) viewModel.loadSocialLinks(gameId)
    }

    val socialLink = socialLinksData?.socialLinks?.find {
        it.arcana.equals(arcana, ignoreCase = true)
    }

    val gameName = when (gameId) {
        "p3fes" -> "Persona 3 FES"; "p3p" -> "Persona 3 Portable"
        "p3r"   -> "Persona 3 Reload"; "p4" -> "Persona 4"
        "p4g"   -> "Persona 4 Golden"; "p5" -> "Persona 5"
        "p5r"   -> "Persona 5 Royal"; else -> "Persona"
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
                            com.persona.companion.utils.ShareUtils.shareSocialLink(context, socialLink, gameName)
                        }) {
                            Icon(Icons.Default.Share, "Share", tint = TextSecondary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        if (socialLink != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Details card (schedule / location)
                socialLink.details?.let { details ->
                    item { DetailsCard(details = details) }
                }
                // Rank cards
                items(socialLink.ranks) { rank ->
                    RankCard(rank = rank)
                }
            }
        } else {
            Box(Modifier.fillMaxSize().padding(padding)) {
                Text("Social Link not found", color = TextSecondary, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun DetailsCard(details: SocialLinkDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            details.trigger?.let   { InfoRow("Trigger",     it) }
            details.schedule?.let  { InfoRow("Schedule",    it) }
            details.timeOfDay?.let { InfoRow("Time of Day", it) }
            details.location?.let  { InfoRow("Location",    it) }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary,
            modifier = Modifier.weight(0.35f))
        Text(value, style = MaterialTheme.typography.bodySmall, color = TextPrimary,
            modifier = Modifier.weight(0.65f))
    }
}

@Composable
private fun RankCard(rank: SocialLinkRank) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            // Header row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(rank.rankName, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                if (rank.isAuto) {
                    Box(Modifier.background(AccentGreen.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("Auto", style = MaterialTheme.typography.bodySmall,
                            color = AccentGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Requirements
            rank.requirements?.let {
                Spacer(Modifier.height(6.dp))
                Text("Requirements: $it", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }

            // Points needed
            if (rank.nextRankPoints > 0) {
                Spacer(Modifier.height(4.dp))
                Text("Points needed: ${rank.nextRankPoints}", style = MaterialTheme.typography.bodyMedium, color = AccentBlue)
            }

            // Dialogues
            if (rank.dialogues.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = TextSecondary.copy(alpha = 0.15f))
                Spacer(Modifier.height(10.dp))
                rank.dialogues.forEach { dialogue ->
                    DialogueBlock(dialogue = dialogue)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DialogueBlock(dialogue: SocialLinkDialogue) {
    // Only show question label if it's meaningful (not generic "Dialogue N")
    val showLabel = !dialogue.question.matches(Regex("Dialogue \\d+"))
    if (showLabel) {
        Text(dialogue.question, style = MaterialTheme.typography.bodySmall,
            color = TextSecondary, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp))
    }
    dialogue.choices.forEach { choice ->
        DialogueChoiceItem(choice = choice)
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun DialogueChoiceItem(choice: DialogueChoice) {
    val bgColor = when {
        choice.points >= 10 -> AccentGreen.copy(alpha = 0.12f)
        choice.points > 0   -> AccentBlue.copy(alpha = 0.10f)
        else                -> Surface.copy(alpha = 0.5f)
    }
    val badgeColor = when {
        choice.points >= 10 -> AccentGreen
        choice.points > 0   -> AccentBlue
        else                -> TextSecondary
    }
    Row(
        modifier = Modifier.fillMaxWidth().background(bgColor, RoundedCornerShape(8.dp)).padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            if (choice.isPhoneChoice) {
                Text("Phone", style = MaterialTheme.typography.labelSmall, color = AccentBlue)
                Spacer(Modifier.height(2.dp))
            }
            Text(choice.text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }
        Spacer(Modifier.width(10.dp))
        Box(Modifier.background(badgeColor, RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
            Text(
                text = if (choice.points > 0) "+${choice.points}" else "—",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}
