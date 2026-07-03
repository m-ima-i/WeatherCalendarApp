# ==========================================
# R8 / ProGuard rules for WeatherCalendarApp
# ==========================================

# デバッグ用: スタックトレースに行番号を保持
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ==========================================
# Gson
# ==========================================
# @SerializedName 付きフィールドを保持
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Gson が使うリフレクション用
-keepattributes Signature
-keepattributes *Annotation*

# Remote Model classes（Gson でシリアライズ/デシリアライズ）
-keep class com.anri.weathercalendarapp.weather.remote.model.** { *; }
-keep class com.anri.weathercalendarapp.calendar.remote.model.** { *; }

# ==========================================
# Retrofit
# ==========================================
# Retrofit の API interface メソッドを保持
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ==========================================
# OkHttp
# ==========================================
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ==========================================
# Kotlin
# ==========================================
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ==========================================
# Kotlinx Serialization（Navigation Compose type-safe routes）
# ==========================================
# @Serializable クラスの Companion と serializer() を保持
-keepclassmembers class com.anri.weathercalendarapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.anri.weathercalendarapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# 生成される $$serializer クラスを保持
-keep,includedescriptorclasses class com.anri.weathercalendarapp.**$$serializer { *; }
