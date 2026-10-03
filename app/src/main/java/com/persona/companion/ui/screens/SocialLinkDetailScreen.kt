package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
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

enum class SocialLinkRouteFilter(val label: String) {
    ALL("All Ranks"),
    ROMANCE("♥ Romance"),
    PLATONIC("✦ Platonic")
}

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

    var selectedRoute by remember(arcana) { mutableStateOf(SocialLinkRouteFilter.ALL) }

    LaunchedEffect(gameId) {
        if (socialLinksData == null) viewModel.loadSocialLinks(gameId)
    }

    val socialLink = socialLinksData?.socialLinks?.find {
        it.arcana.equals(arcana, ignoreCase = true)
    }

    val filteredRanks = remember(socialLink, selectedRoute) {
        val allRanks = socialLink?.ranks ?: emptyList()
        when (selectedRoute) {
            SocialLinkRouteFilter.ROMANCE -> allRanks.filter { !it.isPlatonicRoute }
            SocialLinkRouteFilter.PLATONIC -> allRanks.filter { !it.isRomanceRoute }
            SocialLinkRouteFilter.ALL -> allRanks
        }
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

    val characterName = socialLink?.characterName ?: com.persona.companion.utils.ConfidantHelper.getCharacterName(gameId, arcana)

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (!characterName.isNullOrBlank()) "$arcana — $characterName" else arcana,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
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
                // Character Header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (!characterName.isNullOrBlank()) characterName else arcana,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(primaryColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = arcana.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                }

                                if (socialLink.isP5RExclusive) {
                                    Box(
                                        modifier = Modifier
                                            .background(Persona5Red.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text("P5R EXCLUSIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Persona5Red)
                                    }
                                } else if (socialLink.isP4GExclusive) {
                                    Box(
                                        modifier = Modifier
                                            .background(Persona4Yellow.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text("P4G EXCLUSIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Persona4Yellow)
                                    }
                                }
                            }
                        }
                    }
                }

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

                // Branching route filter tabs (if applicable)
                if (socialLink.hasRouteBranches) {
                    item {
                        RouteSelectorCard(
                            selectedRoute = selectedRoute,
                            primaryColor = primaryColor,
                            onRouteSelected = { selectedRoute = it }
                        )
                    }
                }

                // Rank cards
                items(filteredRanks) { rank ->
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
private fun RouteSelectorCard(
    selectedRoute: SocialLinkRouteFilter,
    primaryColor: Color,
    onRouteSelected: (SocialLinkRouteFilter) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STORY ROUTE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Tap to isolate route",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SocialLinkRouteFilter.values().forEach { filter ->
                    val isSelected = selectedRoute == filter
                    val btnBg = when {
                        isSelected && filter == SocialLinkRouteFilter.ROMANCE -> Color(0xFFE11D48).copy(alpha = 0.22f)
                        isSelected && filter == SocialLinkRouteFilter.PLATONIC -> Color(0xFF0284C7).copy(alpha = 0.22f)
                        isSelected -> primaryColor.copy(alpha = 0.20f)
                        else -> Surface.copy(alpha = 0.7f)
                    }
                    val btnTextColor = when {
                        isSelected && filter == SocialLinkRouteFilter.ROMANCE -> Color(0xFFFB7185)
                        isSelected && filter == SocialLinkRouteFilter.PLATONIC -> Color(0xFF38BDF8)
                        isSelected -> primaryColor
                        else -> TextSecondary
                    }
                    val btnBorder = if (isSelected) {
                        when (filter) {
                            SocialLinkRouteFilter.ROMANCE -> Color(0xFFE11D48)
                            SocialLinkRouteFilter.PLATONIC -> Color(0xFF0284C7)
                            else -> primaryColor
                        }
                    } else Hairline

                    Surface(
                        onClick = { onRouteSelected(filter) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = btnBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, btnBorder)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = btnTextColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RankCard(rank: SocialLinkRank, primaryColor: androidx.compose.ui.graphics.Color) {
    val cardBorder = when {
        rank.isRomanceRoute -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE11D48).copy(alpha = 0.40f))
        rank.isPlatonicRoute -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.40f))
        else -> androidx.compose.foundation.BorderStroke(1.dp, Hairline)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = cardBorder
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            // Header row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rank.cleanRankTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (rank.isRomanceRoute) {
                        Box(
                            Modifier.background(Color(0xFFE11D48).copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFFE11D48).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "♥ Romance Route",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFB7185),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    } else if (rank.isPlatonicRoute) {
                        Box(
                            Modifier.background(Color(0xFF0284C7).copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "✦ Platonic Route",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (rank.rankName.contains("(ALT)", ignoreCase = true)) {
                        Box(
                            Modifier.background(Surface.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "Alt",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp
                            )
                        }
                    }

                    if (rank.isAuto) {
                        Box(
                            Modifier.background(AccentGreen.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "Auto",
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
    val qText = dialogue.question.trim()
    val displayLabel = when {
        qText.matches(Regex("(?i)Dialogue\\s*(\\d+)")) -> {
            val num = Regex("(?i)Dialogue\\s*(\\d+)").find(qText)?.groupValues?.get(1) ?: "1"
            "Choice $num"
        }
        qText.isNotBlank() -> qText
        else -> null
    }

    if (displayLabel != null) {
        Text(
            text = displayLabel,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp, top = 2.dp)
        )
    }
    dialogue.choices.forEach { choice ->
        DialogueChoiceItem(choice = choice)
        Spacer(Modifier.height(6.dp))
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
        choice.isRomanceFlag -> Color(0xFFE11D48).copy(alpha = 0.08f)
        choice.isPlatonicFlag -> Color(0xFF0284C7).copy(alpha = 0.08f)
        choice.points >= 15 -> AccentGreen.copy(alpha = 0.12f)
        choice.points > 0   -> AccentBlue.copy(alpha = 0.10f)
        else                -> Surface.copy(alpha = 0.5f)
    }
    val badgeColor = when {
        choice.points >= 15 -> AccentGreen
        choice.points > 0   -> AccentBlue
        else                -> TextSecondary.copy(alpha = 0.6f)
    }
    val itemBorder = when {
        choice.isRomanceFlag -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE11D48).copy(alpha = 0.35f))
        choice.isPlatonicFlag -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.30f))
        choice.isOptimal -> androidx.compose.foundation.BorderStroke(1.dp, AccentGreen.copy(alpha = 0.40f))
        else -> androidx.compose.foundation.BorderStroke(1.dp, Hairline)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(itemBorder, RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            val hasBadge = choice.isPhoneChoice || choice.isRomanceFlag || choice.isPlatonicFlag || choice.isOptimal || !choice.flag.isNullOrBlank()
            if (hasBadge) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    if (choice.isPhoneChoice) {
                        Box(
                            Modifier.background(AccentBlue.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Phone", style = MaterialTheme.typography.labelSmall, color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }

                    if (choice.isRomanceFlag) {
                        val flagText = choice.flag ?: "Romance Flag"
                        Box(
                            Modifier.background(Color(0xFFE11D48).copy(alpha = 0.20f), RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFFE11D48).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "♥ $flagText",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFB7185),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        }
                    } else if (choice.isPlatonicFlag) {
                        val flagText = choice.flag ?: "Platonic Route"
                        Box(
                            Modifier.background(Color(0xFF0284C7).copy(alpha = 0.20f), RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "✦ $flagText",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    } else if (choice.isOptimal) {
                        Box(
                            Modifier.background(AccentGreen.copy(alpha = 0.20f), RoundedCornerShape(4.dp))
                                .border(1.dp, AccentGreen.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "★ Optimal",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    } else if (!choice.flag.isNullOrBlank()) {
                        Box(
                            Modifier.background(Surface.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = choice.flag,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Text(
                text = choice.text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = if (choice.points >= 15 || choice.isRomanceFlag) FontWeight.SemiBold else FontWeight.Normal
            )
        }

        Spacer(Modifier.width(10.dp))

        Box(
            Modifier.background(badgeColor, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (choice.points > 0) "+${choice.points} pts" else "0 pts",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 11.sp
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
