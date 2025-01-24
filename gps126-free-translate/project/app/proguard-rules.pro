# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keep class com.translate.languagetranslator.freetranslation.database.TranslationDb_Impl { *; }

-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}
-keep class android.net.http.AndroidHttpClient { *; }
-keep class org.apache.http.client.HttpClient { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }

#-keep class com.weewoo.sdkproject.** { *; }

-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

-dontwarn okhttp3.**

-keep class com.translate.languagetranslator.freetranslation.models.** { *; }
-keep class com.translate.languagetranslator.freetranslation.views.** { *; }
-keep class com.translate.languagetranslator.freetranslation.AppBase



# Keep filenames and line numbers for stack traces
-keepattributes SourceFile,LineNumberTable
# Keep JavascriptInterface for WebView bridge
-keepattributes JavascriptInterface
# Sometimes keepattributes is not enough to keep annotations
-keep class android.webkit.JavascriptInterface {
   *;
}


-keepattributes Signature,InnerClasses,Exceptions,Annotation

#-keep public class com.adjust.sdk.** { *; }

-keep public class com.android.installreferrer.** { *; }

#quimera
-keep class com.weewoo.quimera.**  { *; }

#inmobi
#-keepattributes SourceFile,LineNumberTable
#-keep class com.inmobi.** { *; }
#-dontwarn com.inmobi.**
#-keep public class com.google.android.gms.**
#-dontwarn com.google.android.gms.**
#-dontwarn com.squareup.picasso.**
#-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient{
#     public *;
#}
#-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info{
#     public *;
#}
# skip the Picasso library classes
#-keep class com.squareup.picasso.** {*;}
#-dontwarn com.squareup.picasso.**
#-dontwarn com.squareup.okhttp.**
# skip Moat classes
#keep class com.moat.** {*;}
#-dontwarn com.moat.**
# skip AVID classes
#-keep class com.integralads.avid.library.* {*;}

-keep class com.adapty.** { *; }