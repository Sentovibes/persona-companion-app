package com.persona.companion.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory

/**
 * Cast button for Chromecast integration
 * Shows the standard Google Cast icon that users can tap to connect
 */
@Composable
fun CastButton() {
    val context = LocalContext.current
    
    AndroidView(
        factory = { ctx ->
            MediaRouteButton(ctx).apply {
                CastButtonFactory.setUpMediaRouteButton(ctx, this)
            }
        }
    )
}
