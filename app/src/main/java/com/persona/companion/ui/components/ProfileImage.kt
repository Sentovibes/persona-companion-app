package com.persona.companion.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.persona.companion.ui.theme.Background
import com.persona.companion.ui.theme.Surface
import com.persona.companion.ui.theme.TextPrimary
import com.persona.companion.utils.ImageUtils

@Composable
fun ProfileImage(
    name: String,
    isEnemy: Boolean,
    modifier: Modifier = Modifier,
    size: Int = 64
) {
    val context = LocalContext.current
    var showFullImage by remember { mutableStateOf(false) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // Load image
    LaunchedEffect(name) {
        val imagePath = ImageUtils.getImagePath(name, isEnemy)
        bitmap = ImageUtils.loadImageFromAssets(context, imagePath)
    }
    
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Surface)
            .clickable(enabled = bitmap != null) { showFullImage = true },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder icon if no image
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "No image",
                tint = TextPrimary.copy(alpha = 0.5f),
                modifier = Modifier.size((size * 0.6).dp)
            )
        }
    }
    
    // Full screen image dialog
    if (showFullImage && bitmap != null) {
        Dialog(
            onDismissRequest = { showFullImage = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background.copy(alpha = 0.95f))
                    .clickable { showFullImage = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Close button
                    IconButton(
                        onClick = { showFullImage = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Full size image
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = name,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Name label
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
