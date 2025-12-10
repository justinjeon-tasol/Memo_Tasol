# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Room entities
-keep class com.fileshare.app.data.local.entity.** { *; }

# Keep data classes
-keep class com.fileshare.app.domain.model.** { *; }

# Kotlinx serialization (if needed in future)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep generic signatures
-keepattributes Signature
