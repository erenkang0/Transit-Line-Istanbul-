# Keep kotlinx.serialization generated serializers for our model classes.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Our @Serializable seed / import-export models live in these packages.
-keep,includedescriptorclasses class com.transitline.istanbul.**$$serializer { *; }
-keepclassmembers class com.transitline.istanbul.** {
    *** Companion;
}
-keepclasseswithmembers class com.transitline.istanbul.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room generated implementations.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**
