# Nexora+ R8 rules

-keepattributes *Annotation*, InnerClasses, Signature

# kotlinx.serialization
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.mahirex.nexoraplus.**$$serializer { *; }
-keepclassmembers class com.mahirex.nexoraplus.** { *** Companion; }
-keepclasseswithmembers class com.mahirex.nexoraplus.** { kotlinx.serialization.KSerializer serializer(...); }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Widget
-keep class com.mahirex.nexoraplus.widget.** { *; }
