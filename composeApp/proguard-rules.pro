# Add project specific ProGuard rules here.
# See http://developer.android.com/guide/developing/tools/proguard.html

# --- Kotlin Serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `Companion` object fields of serializable classes.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serializable classes and their members
-if @kotlinx.serialization.Serializable class **
-keep class <1> { *; }

# --- Ktor ---
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.**

# --- Koin ---
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# --- General ---
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations
-dontwarn kotlin.**
-dontwarn kotlinx.**
