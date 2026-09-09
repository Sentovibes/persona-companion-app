package com.persona.companion.ui.components

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.persona.companion.R
import com.persona.companion.ui.theme.Background
import com.persona.companion.ui.theme.Surface
import com.persona.companion.ui.theme.TextPrimary
import com.persona.companion.utils.enemyImage
import com.persona.companion.utils.personaImage

@Composable
fun ProfileImage(
    name: String,
    isEnemy: Boolean,
    modifier: Modifier = Modifier,
    size: Int = 64,
    gameId: String? = null,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showFullImage by remember { mutableStateOf(false) }

    val model = remember(name, gameId) {
        val builder = ImageRequest.Builder(context)
        if (isEnemy) builder.enemyImage(context, gameId ?: "", name)
        else builder.personaImage(context, gameId ?: "", name)
        builder.crossfade(true).build()
    }

    var hasImage by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Surface)
            .clickable(enabled = hasImage) {
                if (onClick != null) onClick()
                else showFullImage = true
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = model,
            contentDescription = name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = androidx.compose.ui.res.painterResource(
                if (isEnemy) R.drawable.placeholder_enemy else R.drawable.placeholder_persona
            ),
            error = androidx.compose.ui.res.painterResource(
                if (isEnemy) R.drawable.placeholder_enemy else R.drawable.placeholder_persona
            ),
            onSuccess = { 
                hasImage = true
                isLoading = false
            },
            onError = {
                hasImage = false
                isLoading = false
            },
            onLoading = {
                isLoading = true
                hasImage = false
            }
        )
    }

    // Full screen dialog
    if (showFullImage && hasImage && onClick == null) {
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
                    IconButton(
                        onClick = { showFullImage = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = model,
                        contentDescription = name,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(text = name, style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                }
            }
        }
    }
}
