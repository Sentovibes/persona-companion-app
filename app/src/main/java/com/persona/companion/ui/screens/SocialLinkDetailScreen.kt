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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Add theme color for the game
    val primaryColor = when (gameId) {
        "p5", "p5r" -> Persona5Red
        "p3", "p3p", "p3r" -> Persona3Blue
        "p4", "p4g" -> Persona4Yellow
        else -> TextPrimary
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

                // Ultimate Persona card (if Rank 10 is reached/displayed)
                socialLink.ultimatePersona?.let { persona ->
                    item {
                        UltimatePersonaCard(name = persona, color = primaryColor)
                    }
                }

                // Rank cards
                items(socialLink.ranks) { rank ->
                    RankCard(rank = rank, primaryColor = primaryColor)
                }

                // Third Awakening card (The final evolution)
                socialLink.thirdAwakening?.let { awakening ->
                    item {
                        ThirdAwakeningCard(
                            awakening = awakening,
                            gameId = gameId,
                            color = primaryColor
                        )
                    }
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
private fun RankCard(rank: SocialLinkRank, primaryColor: androidx.compose.ui.graphics.Color) {
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

            // Benefit Card (The new feature!)
            rank.benefit?.let { benefit ->
                Spacer(Modifier.height(12.dp))
                BenefitCard(benefit = benefit, color = primaryColor)
            }

            // Unlocks (New!)
            rank.unlocks?.let { unlocks ->
                if (unlocks.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    UnlocksCard(unlocks = unlocks, color = primaryColor)
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
private fun BenefitCard(benefit: com.persona.companion.models.SocialLinkBenefit, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "★",
                        color = color,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = benefit.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = color,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = benefit.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun UnlocksCard(unlocks: List<String>, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "UNLOCKED",
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            unlocks.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 2.dp)) {
                    Box(Modifier.size(4.dp).background(TextSecondary, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(8.dp))
                    Text(item, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun UltimatePersonaCard(name: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                Modifier.background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text("MAX", color = color, fontWeight = FontWeight.ExtraBold)
            }
            Column {
                Text("Ultimate Persona Unlock", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(name, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
@Composable
private fun DialogueChoiceItem(choice: com.persona.companion.models.DialogueChoice) {
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
                text = if (choice.points > 0) "${choice.points}" else "—",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}
@Composable
private fun ThirdAwakeningCard(
    awakening: com.persona.companion.models.ThirdAwakening,
    gameId: String,
    color: androidx.compose.ui.graphics.Color
) {
    val tagText = if (gameId == "p5r") "Third Semester" else "Winter Event"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, color.copy(alpha = 0.5f))
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Third Awakening",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
                Box(
                    Modifier.background(color.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        tagText,
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = awakening.persona,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                awakening.name?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Bold)
                }
                awakening.description?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            
            awakening.requirement?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Requirement: $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}
