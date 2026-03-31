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

# MDMobile specific rules

# 保留应用类
-keep class com.example.mdmobile.** { *; }

# 保留必要的AndroidX和Kotlin类
-keep class androidx.** { *; }
-keep class kotlin.** { *; }

# 保留注解和序列化
-keepattributes *Annotation*, Signature, InnerClasses

# 保留资源ID
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 保留Compose相关类
-keep class androidx.compose.** { *; }

# 保留Navigation组件
-keep class androidx.navigation.** { *; }

# 保留Room数据库组件
-keep class androidx.room.** { *; }

# 保留DataStore组件
-keep class androidx.datastore.** { *; }