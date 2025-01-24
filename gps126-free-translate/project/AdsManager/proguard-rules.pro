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
#-keepattributes SourceFile,LineNumberTable
#-renamesourcefileattribute SourceFile
#
##Ironsource and mediators
#-keepclassmembers class com.ironsource.sdk.controller.IronSourceWebView$JSInterface {
#    public *;
#}
#-keepclassmembers class * implements android.os.Parcelable {
#    public static final android.os.Parcelable$Creator *;
#}
#-keep public class com.google.android.gms.ads.** {
#   public *;
#}
#-keep class com.ironsource.adapters.** { *;
#}
#-dontwarn com.ironsource.mediationsdk.**
#-dontwarn com.ironsource.adapters.**
#-keepattributes JavascriptInterface
#-keepclassmembers class * {
#    @android.webkit.JavascriptInterface <methods>;
#}
#
## Keep filenames and line numbers for stack traces
#-keepattributes SourceFile,LineNumberTable
## Keep JavascriptInterface for WebView bridge
#-keepattributes JavascriptInterface
## Sometimes keepattributes is not enough to keep annotations
#-keep class android.webkit.JavascriptInterface {
#   *;
#}
## Keep all classes in Unity Ads package
#-keep class com.unity3d.ads.** {
#   *;
#}
## Keep all classes in Unity Services package
#-keep class com.unity3d.services.** {
#   *;
#}
#-dontwarn com.google.ar.core.**
#-dontwarn com.unity3d.services.**
#-dontwarn com.ironsource.adapters.unityads.**
#
#-keepattributes Signature,InnerClasses,Exceptions,Annotation
#-keep public class com.applovin.sdk.AppLovinSdk{ *; }
#-keep public class com.applovin.sdk.AppLovin* { public protected *; }
#-keep public class com.applovin.nativeAds.AppLovin* { public protected *; }
#-keep public class com.applovin.adview.* { public protected *; }
#-keep public class com.applovin.mediation.* { public protected *; }
#-keep public class com.applovin.mediation.ads.* { public protected *; }
#-keep public class com.applovin.impl.*.AppLovin { public protected *; }
#-keep public class com.applovin.impl.**.*Impl { public protected *; }
#-keepclassmembers class com.applovin.sdk.AppLovinSdkSettings { private java.util.Map localSettings; }
#-keep class com.applovin.mediation.adapters.** { *; }
#-keep class com.applovin.mediation.adapter.**{ *; }
#-keep class com.amazon.device.ads.** { *; }
#-dontwarn com.facebook.ads.internal.**
#-keeppackagenames com.facebook.*
#-keep public class com.facebook.ads.** {*;}
#-keep public class com.facebook.ads.**
#{ public protected *; }
#
## For communication with AdColony's WebView
#-keepclassmembers class * {
#    @android.webkit.JavascriptInterface <methods>;
#}
## Keep ADCNative class members unobfuscated
#-keepclassmembers class com.adcolony.sdk.ADCNative** {
#    *;
# }
#
# # Vungle
# -keep class com.vungle.warren.** { *; }
# -dontwarn com.vungle.warren.error.VungleError$ErrorCode
# # Moat SDK
# -keep class com.moat.** { *; }
# -dontwarn com.moat.**
# # Okio
# -dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
# # Retrofit
# -dontwarn okio.**
# -dontwarn retrofit2.Platform$Java8
# # Gson
# -keepattributes Signature
# -keepattributes *Annotation*
# -dontwarn sun.misc.**
# -keep class com.google.gson.examples.android.model.** { *; }
# -keep class * implements com.google.gson.TypeAdapterFactory
# -keep class * implements com.google.gson.JsonSerializer
# -keep class * implements com.google.gson.JsonDeserializer
# # Google Android Advertising ID
# -keep class com.google.android.gms.internal.** { *; }
# -dontwarn com.google.android.gms.ads.identifier.**
#
# -keep class com.bytedance.sdk.** { *; }
#
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mbridge.** {*; }
-keep interface com.mbridge.** {*; }
-keep class android.support.v4.** { *; }
-dontwarn com.mbridge.**
-keep class **.R$* { public static final int mbridge*; }
-keep public class com.mbridge.* extends androidx.** { *; }
-keep public class androidx.viewpager.widget.PagerAdapter{ *; }
-keep public class androidx.viewpager.widget.ViewPager.OnPageChangeListener{ *; }
-keep interface androidx.annotation.IntDef{ *; }
-keep interface androidx.annotation.Nullable{ *; }
-keep interface androidx.annotation.CheckResult{ *; }
-keep interface androidx.annotation.NonNull{ *; }
-keep public class androidx.fragment.app.Fragment{ *; }
-keep public class androidx.core.content.FileProvider{ *; }
-keep public class androidx.core.app.NotificationCompat{ *; }
-keep public class androidx.appcompat.widget.AppCompatImageView { *; }
-keep public class androidx.recyclerview.*{ *; }
-keep class com.translate.languagetranslator.freetranslation.database.TranslationDb_Impl { *; }
