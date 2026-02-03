# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ============================================================================
# Room Database - Keep entities and DAOs from obfuscation
# ============================================================================

# Keep all database entities (Room requires exact field names)
-keep class com.example.english.data.database.entity.** { *; }
-keep class com.example.english.data.model.** { *; }
-keep class com.example.english.data.local.entity.** { *; }

# Keep DAO interfaces (Room uses reflection)
-keep interface com.example.english.data.database.dao.** { *; }
-keep interface com.example.english.data.local.dao.** { *; }

# Keep TypeConverters
-keep class com.example.english.data.database.Converters { *; }

# ============================================================================
# SRS Algorithm - Keep constants used in validation
# ============================================================================

# Keep SRS quality constants (used in require() validation)
-keepclassmembers class com.example.english.data.algorithm.SRSAlgorithm$Companion {
    public static final int QUALITY_*;
}

# ============================================================================
# Creator Signature & Easter Eggs - Preserve intentionally
# ============================================================================

# Keep BuildConfig (contains creator metadata)
-keep class com.example.english.BuildConfig { *; }

# Keep AppLogger (contains creator signature)
-keep class com.example.english.util.AppLogger { *; }

# Keep database metadata methods
-keepclassmembers class com.example.english.data.database.AppDatabase$Companion {
    public ** getCreatorInfo();
    public ** getDatabaseMetadata();
}

# ============================================================================
# Kotlin & AndroidX - Standard rules
# ============================================================================

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ============================================================================
# Debugging - Enable for better crash reports
# ============================================================================

# Keep source file names and line numbers for crash reports
-keepattributes SourceFile,LineNumberTable

# Rename source file to hide original structure
-renamesourcefileattribute SourceFile