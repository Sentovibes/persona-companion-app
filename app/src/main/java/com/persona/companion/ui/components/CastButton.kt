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
    AndroidView(
        factory = { ctx ->
            try {
                MediaRouteButton(ctx).apply {
                    CastButtonFactory.setUpMediaRouteButton(ctx, this)
                }
            } catch (e: Exception) {
                // Cast not available, return empty view
                MediaRouteButton(ctx)
            }
        }
    )
}
