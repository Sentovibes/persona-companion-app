package com.persona.companion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.persona.companion.ui.theme.*

@Composable
fun ElementTag(
    element: String,
    modifier: Modifier = Modifier
) {
    val color = when (element.lowercase()) {
        "phys", "physical", "slash", "strike", "pierce" -> TagPhys
        "fire" -> TagFire
        "ice" -> TagIce
        "elec", "electric" -> TagElec
        "wind" -> TagWind
        "psy", "psychic" -> TagPsychic
        "nuke", "nuclear" -> TagNuclear
        "bless", "light" -> TagBless
        "curse", "dark" -> TagCurse
        "almighty" -> TagAlmighty
        else -> Color.Gray
    }

    val textColor = if (color == TagBless || color == TagElec) Color.Black else Color.White

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.9f))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = element.take(2).uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
fun WeaknessRow(
    weaknesses: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        weaknesses.take(4).forEach { element ->
            ElementTag(element)
        }
        if (weaknesses.size > 4) {
            Text(
                text = "+${weaknesses.size - 4}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}
