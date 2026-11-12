package com.persona.companion.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.persona.companion.ui.theme.*

data class WhatsNewItem(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun WhatsNewDialog(
    versionName: String = "8.1",
    versionCode: Int = 64,
    onDismiss: () -> Unit
) {
    val items = listOf(
        WhatsNewItem(
            title = "Pin & Mark Maxed Social Links",
            description = "Pin active Confidants and Social Links to the top so you can check hangout answers without scrolling. Mark finished ones as Maxed to move them out of the way.",
            icon = Icons.Default.PushPin
        ),
        WhatsNewItem(
            title = "All 962 Artworks Included Offline",
            description = "Persona and Enemy images now ship built into the app using WebP (~30 MB total), so you no longer need a separate 1 GB download.",
            icon = Icons.Default.Image
        ),
        WhatsNewItem(
            title = "Export & Restore Backups",
            description = "Save your favorites, pinned Social Links, completed requests, and settings to a file, or move them between Android and the Web app.",
            icon = Icons.Default.ImportExport
        ),
        WhatsNewItem(
            title = "Skill Inheritance Rules",
            description = "See which skill types a Persona cannot inherit and how many inherited slots carry over directly on its detail screen.",
            icon = Icons.Default.AutoAwesome
        ),
        WhatsNewItem(
            title = "UI & Crash Fixes",
            description = "Sharper card borders across dark surfaces, smoother expandable lists, and fixes for Android 15 edge-to-edge display and layout crashes.",
            icon = Icons.Default.CheckCircle
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 620.dp),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceCard,
            border = BorderStroke(1.dp, HairlineStrong),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "What's New in v$versionName",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SurfaceRaised,
                            border = BorderStroke(1.dp, Hairline)
                        ) {
                            Text(
                                text = "Build $versionCode",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = AccentBrass,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    thickness = 1.dp,
                    color = Divider
                )

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items.forEachIndexed { index, item ->
                        WhatsNewRow(item)
                        if (index < items.lastIndex) {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = HairlineSoft
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBrass,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun WhatsNewRow(item: WhatsNewItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = AccentBrass,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                color = TextSecondary
            )
        }
    }
}
