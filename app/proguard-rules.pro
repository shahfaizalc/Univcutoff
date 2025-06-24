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

#-repackageclasses com.wibmo.addons

#Keep class name but allow method obfuscation
-keeppackagenames com.faikan.univcounselling.**

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable
-keep class com.faikan.univcounselling.model { *; }


# Keep `Companion` object fields of serializable classes.
# This is also needed for R8 in compat mode since multiple
# optimizations will remove the generic signature such as class
# merging and argument removal. See:
# https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md#troubleshooting-gson-gson

# Optional. For using GSON @Expose annotation
-keepattributes AnnotationDefault,RuntimeVisibleAnnotations

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


# For using GSON @Expose annotation
-keepattributes *Annotation*

# Keep TypeToken and its subclasses
#-keep class com.google.gson.reflect.TypeToken { *; }
#-keep class * extends com.google.gson.reflect.TypeToken
-dontwarn com.facebook.infer.annotation.**
-keep class com.facebook.infer.annotation.** { *; }