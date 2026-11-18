package com.persona.companion.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.persona.companion.R
import com.persona.companion.ui.theme.Background
import com.persona.companion.ui.theme.Hairline
import com.persona.companion.ui.theme.SurfaceRaised
import com.persona.companion.ui.theme.TextDisabled
import com.persona.companion.ui.theme.TextPrimary
import com.persona.companion.ui.theme.TextSecondary
import com.persona.companion.utils.ResistanceUtils

fun elementIconRes(element: String): Int? = when (element.trim().lowercase()) {
    "fire" -> R.drawable.ic_fire
    "ice" -> R.drawable.ic_ice
    "elec", "electric" -> R.drawable.ic_elec
    "wind", "force" -> R.drawable.ic_wind
    "light", "bless" -> R.drawable.ic_light
    "dark", "curse" -> R.drawable.ic_dark
    "almighty" -> R.drawable.ic_almighty
    "psy", "psychic" -> R.drawable.ic_psy
    "nuclear", "nuke" -> R.drawable.ic_nuke
    "slash" -> R.drawable.ic_slash
    "gun", "pierce" -> R.drawable.ic_gun
    "phys", "physical", "strike" -> R.drawable.ic_phys
    "recovery", "heal" -> R.drawable.ic_recovery
    "support" -> R.drawable.ic_support
    "ailment", "ailments" -> R.drawable.ic_ailment
    "passive" -> R.drawable.ic_passive
    else -> null
}

fun elementIconTint(element: String): Color = when (element.trim().lowercase()) {
    "fire" -> Color(0xFFFF7043)
    "ice" -> Color(0xFF4FC3F7)
    "elec", "electric" -> Color(0xFFFDD835)
    "wind", "force" -> Color(0xFF66BB6A)
    "light", "bless" -> Color(0xFFFFF176)
    "dark", "curse" -> Color(0xFFCE93D8)
    "almighty" -> Color(0xFFFFB74D)
    "psy", "psychic" -> Color(0xFFEA80FC)
    "nuclear", "nuke" -> Color(0xFF26C6DA)
    "phys", "physical", "slash", "strike", "pierce", "gun" -> Color(0xFFE0E0E0)
    "recovery", "heal" -> Color(0xFF81C784)
    "support" -> Color(0xFF64B5F6)
    "ailment", "ailments" -> Color(0xFFB0BEC5)
    else -> TextSecondary
}

private fun elementShortCode(element: String): String = when (element.trim().lowercase()) {
    "slash" -> "SLA"
    "strike" -> "STR"
    "pierce" -> "PIE"
    "phys", "physical" -> "PHY"
    "gun" -> "GUN"
    "fire" -> "FIR"
    "ice" -> "ICE"
    "elec", "electric" -> "ELE"
    "wind", "force" -> "WND"
    "psy", "psychic" -> "PSY"
    "nuclear", "nuke" -> "NUK"
    "light" -> "LGT"
    "bless" -> "BLS"
    "dark" -> "DRK"
    "curse" -> "CRS"
    "almighty" -> "ALM"
    else -> element.take(3).uppercase()
}

fun affinityCategoryColor(category: String): Color = when (category.trim().lowercase()) {
    "weak" -> Color(0xFFE57373)
    "resist", "resists" -> Color(0xFF81C784)
    "null", "block" -> Color(0xFFCFD8DC)
    "repel", "reflect" -> Color(0xFF64B5F6)
    "absorb", "drain" -> Color(0xFFFFD54F)
    else -> TextSecondary
}

private fun affinityBadgeCode(category: String): String = when (category.trim().lowercase()) {
    "weak" -> "Wk"
    "resist", "resists" -> "Rs"
    "null", "block" -> "Nu"
    "repel", "reflect" -> "Rp"
    "absorb", "drain" -> "Dr"
    else -> "—"
}

private fun elementAliases(element: String): Set<String> = when (element.trim().lowercase()) {
    "light", "bless" -> setOf("light", "bless")
    "dark", "curse" -> setOf("dark", "curse")
    "nuclear", "nuke" -> setOf("nuclear", "nuke")
    "phys", "physical" -> setOf("phys", "physical")
    "elec", "electric" -> setOf("elec", "electric")
    "psy", "psychic" -> setOf("psy", "psychic")
    else -> setOf(element.trim().lowercase())
}

/**
 * In-game Persona status affinity strip showing every element for the active game edition
 * with its real element icon and canonical affinity tag (Wk / Rs / Nu / Rp / Dr / —).
 */
@Composable
fun AffinityElementMatrix(
    gameId: String,
    grouped: Map<String, List<String>>,
    modifier: Modifier = Modifier
) {
    val elements = ResistanceUtils.getElementNames(gameId)
    val perRow = if (elements.size == 8) 4 else 5
    val rows = elements.chunked(perRow)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        rows.forEach { rowElements ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                rowElements.forEach { element ->
                    val aliases = elementAliases(element)
                    val matchedCategory = listOf("Weak", "Absorb", "Repel", "Null", "Resist")
                        .firstOrNull { cat ->
                            grouped[cat]?.any { it.trim().lowercase() in aliases } == true
                        }
                    val badge = matchedCategory?.let { affinityBadgeCode(it) } ?: "—"
                    val badgeColor = matchedCategory?.let { affinityCategoryColor(it) } ?: TextDisabled
                    val elemTint = elementIconTint(element)
                    val iconRes = elementIconRes(element)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Background.copy(alpha = 0.7f)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceRaised)
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            if (iconRes != null) {
                                Icon(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = element,
                                    tint = elemTint,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = elementShortCode(element),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.4.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (matchedCategory != null) badgeColor.copy(alpha = 0.16f)
                                    else Color.Transparent
                                )
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = badge,
                                fontSize = 12.sp,
                                fontWeight = if (matchedCategory != null) FontWeight.ExtraBold else FontWeight.Normal,
                                color = badgeColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AffinityChip(
    element: String,
    chipColor: Color,
    modifier: Modifier = Modifier,
    qualifier: String? = null
) {
    val iconRes = elementIconRes(element)
    val iconTint = elementIconTint(element)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = chipColor.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, chipColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = element,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            if (!qualifier.isNullOrBlank()) {
                Text(
                    text = qualifier,
                    fontSize = 10.sp,
                    color = chipColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AffinityRow(
    label: String,
    elements: List<String>,
    chipColor: Color = affinityCategoryColor(label)
) {
    if (elements.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = chipColor,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
        FlowRow(
            modifier = Modifier.padding(top = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            elements.forEach { element ->
                AffinityChip(element = element, chipColor = chipColor)
            }
        }
    }
}

@Composable
fun AffinityGroupedSection(
    grouped: Map<String, List<String>>,
    gameId: String = "",
    emptyText: String = "No special affinities"
) {
    if (gameId.isNotBlank()) {
        AffinityElementMatrix(gameId = gameId, grouped = grouped)
        return
    }

    val orderedCategories = listOf("Weak", "Resist", "Null", "Repel", "Absorb")
    val entries = orderedCategories.mapNotNull { cat ->
        val items = grouped[cat]
        if (items.isNullOrEmpty()) null else cat to items
    }

    if (entries.isEmpty()) {
        Text(
            text = emptyText,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        entries.forEach { (category, elements) ->
            AffinityRow(
                label = category,
                elements = elements,
                chipColor = affinityCategoryColor(category)
            )
        }
    }
}

private val KNOWN_ELEMENTS = listOf(
    "Slash", "Strike", "Pierce", "Phys", "Physical", "Gun",
    "Fire", "Ice", "Elec", "Electric", "Wind", "Force",
    "Psy", "Psychic", "Nuclear", "Nuke",
    "Light", "Bless", "Dark", "Curse", "Almighty", "Ailments", "Ailment"
)

private val QUALIFIER_REGEX = Regex("""\(([^)]+)\)""")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AffinityTextChips(
    rawText: String,
    isWeakness: Boolean
) {
    val trimmed = rawText.trim()
    if (trimmed.isEmpty() || trimmed.equals("None", ignoreCase = true)) {
        Text(
            text = if (trimmed.isEmpty()) "None" else trimmed,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        return
    }

    val segments = trimmed.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        segments.forEach { segment ->
            val colonIdx = segment.indexOf(':')
            val prefixLabel = if (colonIdx in 1..28) segment.substring(0, colonIdx).trim() else null
            val body = if (prefixLabel != null) segment.substring(colonIdx + 1).trim() else segment

            if (prefixLabel != null) {
                Text(
                    text = prefixLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (body.equals("None", ignoreCase = true)) {
                Text(
                    text = "None",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                val tokens = body.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val parsedChips = mutableListOf<Triple<String, String?, Color>>()
                val fallbackNotes = mutableListOf<String>()

                tokens.forEach { token ->
                    val qualifierMatch = QUALIFIER_REGEX.find(token)
                    val qualifier = qualifierMatch?.groupValues?.getOrNull(1)?.trim()
                    val cleanToken = token.replace(QUALIFIER_REGEX, "").trim()

                    val slashParts = cleanToken.split("/").map { it.trim() }.filter { it.isNotEmpty() }
                    val allKnown = slashParts.isNotEmpty() && slashParts.all { part ->
                        KNOWN_ELEMENTS.any { it.equals(part, ignoreCase = true) }
                    }

                    if (allKnown) {
                        val chipColor = when {
                            isWeakness -> affinityCategoryColor("Weak")
                            qualifier != null -> affinityCategoryColor(
                                listOf("Null", "Repel", "Absorb", "Drain", "Resist", "Weak")
                                    .firstOrNull { qualifier.contains(it, ignoreCase = true) } ?: "Resist"
                            )
                            else -> affinityCategoryColor("Resist")
                        }
                        slashParts.forEach { elem ->
                            val canonical = KNOWN_ELEMENTS.first { it.equals(elem, ignoreCase = true) }
                            parsedChips += Triple(canonical, qualifier, chipColor)
                        }
                    } else {
                        fallbackNotes += token
                    }
                }

                if (parsedChips.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        parsedChips.forEach { (elem, qual, color) ->
                            AffinityChip(
                                element = elem,
                                chipColor = color,
                                qualifier = qual?.takeIf { !it.equals("Weak", ignoreCase = true) || !isWeakness }
                            )
                        }
                    }
                }
                if (fallbackNotes.isNotEmpty()) {
                    if (parsedChips.isNotEmpty()) Spacer(Modifier.height(2.dp))
                    Text(
                        text = fallbackNotes.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isWeakness) affinityCategoryColor("Weak") else TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
