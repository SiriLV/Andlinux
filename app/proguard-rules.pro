# ============================================================================
# AndLinux proguard rules
# ============================================================================
# Note: minify is currently disabled (isMinifyEnabled = false in
# app/build.gradle.kts), so these rules are inert. They are kept so that
# enabling minify in the future does not break the build.
# ============================================================================

# --- Suppress warnings for classes referenced only on JVM (not Android) ---
-dontwarn java.lang.management.ManagementFactory
-dontwarn javax.management.**
-dontwarn javax.script.**
-dontwarn javax.security.auth.login.LoginContext
-dontwarn javax.servlet.**
-dontwarn javax.swing.**
-dontwarn javax.swing.border.**
-dontwarn javax.swing.event.**
-dontwarn javax.swing.plaf.**
-dontwarn javax.swing.plaf.basic.**
-dontwarn javax.swing.text.**
-dontwarn javax.swing.tree.**
-dontwarn org.ietf.jgss.**
-dontwarn sun.misc.**
-dontwarn sun.security.x509.X509Key

# --- Kotlin / KotlinX reflection guards ---
-dontwarn kotlin.Cloneable$DefaultImpls

# --- JGit (used by some terminal/git integrations if added later) ---
-keep class org.eclipse.jgit.** { *; }
-keep class org.eclipse.jgit.util.SystemReader { *; }
-keep class org.eclipse.jgit.storage.file.FileBasedConfig { *; }
-keep class org.eclipse.**
-dontwarn org.eclipse.**
-dontwarn java.awt.**

# --- Gson (used for serialization in some Compose/state paths) ---
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter { *; }
-keep class * extends com.google.gson.TypeAdapterFactory { *; }
-keep class * implements com.google.gson.TypeAdapterFactory {
    public <init>();
}
-keep class * extends com.google.gson.JsonSerializer { *; }
-keep class * implements com.google.gson.JsonSerializer
-keep class * extends com.google.gson.JsonDeserializer { *; }
-keep class * implements com.google.gson.JsonDeserializer
-keep class * extends com.google.gson.JsonElement { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
-keep class com.google.gson.stream.** { *; }
-dontwarn com.google.gson.**

# Keep @SerializedName-annotated fields.
-keepattributes *Annotation*, Signature
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keepclasseswithmembers class * {
    <init>(...);
    @com.google.gson.annotations.SerializedName <fields>;
}
# Keep no-arg constructors needed by Gson reflection.
-keepclassmembers class * {
    public <init>();
}

# --- AndLinux app entry points ---
-keep class com.rk.terminal.App { *; }
-keep class com.rk.terminal.ui.activities.terminal.MainActivity { *; }
-keep class com.rk.terminal.service.SessionService { *; }
-keep class com.rk.AlpineDocumentProvider { *; }
-keepclasseswithmembernames class com.rk.terminal.App { *; }
-keepclassmembernames class com.rk.terminal.App { *; }
-keepnames class com.rk.terminal.App { *; }

# --- Joni regex (used by terminal-emulator for OSC/CSI parsing) ---
-keep class org.joni.ast.QuantifierNode { *; }
