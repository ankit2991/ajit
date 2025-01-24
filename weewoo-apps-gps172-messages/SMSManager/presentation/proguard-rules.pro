-dontobfuscate

# android-smsmms
# -keep class android.net.** { *; }
-dontwarn android.net.ConnectivityManager
-dontwarn android.net.LinkProperties

# autodispose
-dontwarn com.uber.autodispose.**

# ez-vcard
-dontwarn ezvcard.**
-dontwarn org.apache.log.**
-dontwarn org.apache.log4j.**
-dontwarn org.python.core.**

# okio
-dontwarn okio.**

-dontwarn kotlin.reflect.jvm.internal.**
-keep class kotlin.reflect.jvm.internal.** { *; }
-keep class kotlin.Metadata

# okhttp3
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform


# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}

-keep @com.squareup.moshi.JsonQualifier interface *

# Enum field names are used by the integrated EnumJsonAdapter.
# values() is synthesized by the Kotlin compiler and is used by EnumJsonAdapter indirectly
# Annotate enums with @JsonClass(generateAdapter = false) to use them with Moshi.
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
    **[] values();
}

-keep class com.messaging.textrasms.manager.common.widget.GroupAvatarView
-keep class com.messaging.textrasms.manager.common.widget.AvatarView




#quimera
-keep class com.weewoo.quimera.**  { *; }



