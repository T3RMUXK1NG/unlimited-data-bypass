# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in Android SDK tools.

# Keep all bypass engines
-keep class com.t3rmuxk1ng.unlimiteddatabypass.engines.** { *; }
-keep class com.t3rmuxk1ng.unlimiteddatabypass.services.** { *; }
-keep class com.t3rmuxk1ng.unlimiteddatabypass.models.** { *; }
-keep class com.t3rmuxk1ng.unlimiteddatabypass.config.** { *; }
-keep class com.t3rmuxk1ng.unlimiteddatabypass.utils.** { *; }

# Keep all native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep serialization methods
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Gson
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Encryption
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }
