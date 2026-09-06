# Persona Companion Wear OS Proguard Rules
-keep class com.persona.companion.wear.models.** { *; }

# Keep serialized fields
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod,InnerClasses
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-dontwarn sun.misc.**

-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
