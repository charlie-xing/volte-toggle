-dontobfuscate
-ignorewarnings
-dontwarn androidx.**
-dontwarn org.jetbrains.annotations.**
-dontwarn org.bouncycastle.**
-dontwarn kotlinx.coroutines.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn dalvik.system.**
-dontwarn sun.misc.**

# App entry points (Activity, BroadcastReceivers, Instrumentation)
-keep public class com.bp.pixel_ims_apn_tuner.** { *; }

# HiddenApiBypass relies on exact field layout & MethodHandle stubs
-keep class org.lsposed.hiddenapibypass.** { *; }

# SPAKE2 pure-JVM bridge & implementation
-keep class io.github.muntashirakon.crypto.spake2.** { *; }
-keep class com.flyfish233.crypto.spake2.** { *; }

# Kadb ADB client
-keep class com.flyfishxu.kadb.** { *; }

# BouncyCastle JCA X.509, RSA, and SHA-256/SHA-1 provider classes loaded dynamically by BouncyCastleProvider
-keep class org.bouncycastle.jce.provider.BouncyCastleProvider { *; }
-keep class org.bouncycastle.jcajce.provider.asymmetric.X509* { *; }
-keep class org.bouncycastle.jcajce.provider.asymmetric.x509.** { *; }
-keep class org.bouncycastle.jcajce.provider.asymmetric.RSA* { *; }
-keep class org.bouncycastle.jcajce.provider.asymmetric.rsa.** { *; }
-keep class org.bouncycastle.jcajce.provider.digest.SHA1* { *; }
-keep class org.bouncycastle.jcajce.provider.digest.SHA256* { *; }
-keep class org.bouncycastle.jcajce.provider.digest.SHA512* { *; }
