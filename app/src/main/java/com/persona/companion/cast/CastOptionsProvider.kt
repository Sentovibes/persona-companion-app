package com.persona.companion.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider

/**
 * Cast Options Provider for Google Cast integration
 * This is required by the Cast SDK
 */
class CastOptionsProvider : OptionsProvider {
    
    override fun getCastOptions(context: Context): CastOptions {
        return CastOptions.Builder()
            // Use the default Cast receiver app ID
            // CC1AD845 is the default media receiver
            .setReceiverApplicationId("CC1AD845")
            .build()
    }
    
    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        return null
    }
}
