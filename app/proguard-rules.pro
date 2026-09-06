# Persona Companion proguard rules

# Keep Gson model classes and data models
-keep class com.persona.companion.models.** { *; }
-keep class com.persona.companion.data.UserPreferences$RecentItem { *; }
-keep class com.persona.companion.data.imagedownload.** { *; }
-keep class com.persona.companion.data.database.** { *; }
-keep class com.persona.companion.fusion.** { *; }

# Gson uses reflection on TypeToken
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod,InnerClasses
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# NanoHTTPD
-keep class org.nanohttpd.** { *; }
-dontwarn org.nanohttpd.**

# ZXing
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**
