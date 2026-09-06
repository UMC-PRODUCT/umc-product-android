# ─────────────────────────────────────────────────────────────────────────────
# UMC Product — R8 규칙
#
# 이 앱은 Gson(Retrofit)과 kotlinx.serialization 을 함께 쓴다.
# data class 158개 중 @SerializedName 이 붙은 것은 일부뿐이라, 나머지는
# "필드 이름이 곧 JSON 키"다. 난독화로 필드명이 바뀌면 컴파일은 통과하지만
# 파싱 결과가 조용히 null/기본값이 된다. 아래 keep 규칙은 그 경계를 지킨다.
# ─────────────────────────────────────────────────────────────────────────────

# ── 크래시 스택 추적 ─────────────────────────────────────────────────────────
# mapping.txt 를 Play 에 업로드하므로 줄 번호를 남기고 원본 파일명만 감춘다.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── 제네릭·애노테이션 메타데이터 ─────────────────────────────────────────────
# Gson 의 TypeToken 과 kotlinx.serialization 이 제네릭 시그니처를 런타임에 읽는다.
# 이게 지워지면 ApiResponse<T> 의 T 를 복원하지 못한다.
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# ── 직렬화 경계: 필드명을 그대로 보존해야 하는 패키지 ────────────────────────
-keep class com.umc.data.request.** { *; }
-keep class com.umc.data.response.** { *; }
-keep class com.umc.domain.model.** { *; }

# ── enum: Gson 은 상수 "이름"으로 직렬화한다 ─────────────────────────────────
# domain 에만 enum 이 55개다. 상수명이 바뀌면 서버와 값이 어긋난다.
-keepclassmembers enum * { *; }

# ── kotlinx.serialization ────────────────────────────────────────────────────
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class *
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class * {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Gson ─────────────────────────────────────────────────────────────────────
-dontwarn sun.misc.**
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ── Retrofit / OkHttp ────────────────────────────────────────────────────────
# 최신 Retrofit·OkHttp 는 consumer 규칙을 동봉하지만, suspend 함수의
# Continuation 파라미터와 제네릭 반환 타입은 명시해 두는 편이 안전하다.
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ── Kotlin 메타데이터 ────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
