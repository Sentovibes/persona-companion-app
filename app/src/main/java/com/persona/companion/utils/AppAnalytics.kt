package com.persona.companion.utils

import android.os.Build
import com.persona.companion.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object AppAnalytics {
    private const val UMAMI_API_URL = "https://cloud.umami.is/api/send"
    private const val WEBSITE_ID = "6b5ab9f1-6ef3-4ffb-871b-ba8aa104c51a"
    private const val HOSTNAME = "android.persona-companion.app"

    fun trackAppOpen() {
        trackEvent(
            url = "/app/android/launch",
            title = "Persona Companion APK Open",
            eventName = "app_open",
            eventData = mapOf(
                "app_version" to BuildConfig.VERSION_NAME,
                "os_version" to Build.VERSION.RELEASE,
                "device_manufacturer" to Build.MANUFACTURER,
                "device_model" to Build.MODEL
            )
        )
    }

    fun trackScreen(screenName: String) {
        trackEvent(
            url = "/app/android/$screenName",
            title = "Screen: $screenName",
            eventName = "screen_view",
            eventData = mapOf(
                "screen" to screenName,
                "app_version" to BuildConfig.VERSION_NAME
            )
        )
    }

    private fun trackEvent(
        url: String,
        title: String,
        eventName: String? = null,
        eventData: Map<String, Any>? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val payload = JSONObject().apply {
                    put("type", "event")
                    val p = JSONObject().apply {
                        put("website", WEBSITE_ID)
                        put("hostname", HOSTNAME)
                        put("url", url)
                        put("title", title)
                        if (eventName != null) {
                            put("name", eventName)
                        }
                        if (eventData != null) {
                            val dataJson = JSONObject()
                            eventData.forEach { (k, v) -> dataJson.put(k, v) }
                            put("data", dataJson)
                        }
                    }
                    put("payload", p)
                }

                val connection = URL(UMAMI_API_URL).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}; ${Build.MODEL}) PersonaCompanion/${BuildConfig.VERSION_NAME}")
                connection.doOutput = true
                connection.connectTimeout = 6000
                connection.readTimeout = 6000

                connection.outputStream.use { os ->
                    os.write(payload.toString().toByteArray(Charsets.UTF_8))
                }
                connection.responseCode
                connection.disconnect()
            } catch (_: Exception) {
                // Silently ignore network failures to ensure app runs offline smoothly
            }
        }
    }
}
