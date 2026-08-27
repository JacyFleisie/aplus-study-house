# A+ Study House — release shrinker / obfuscation rules
# Keep the app working after R8 minify + obfuscation.

# ---- Keep data model classes (org.json parsing relies on field names) ----
-keep class com.example.blankapp.data.models.** { *; }
-keep class com.example.blankapp.data.MockUser { *; }
-keep class com.example.blankapp.data.MockStudent { *; }
-keep class com.example.blankapp.data.MockInvoice { *; }

# ---- Hilt / Dagger ----
-keep class dagger.** { *; }
-keep class **_HiltModules* { *; }
-keep class **_Factory { *; }
-keep class **_HiltInjector { *; }
-keep class hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @androidx.hilt.lifecycle.ViewModelInject class * { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# ---- OkHttp / Okio ----
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ---- org.json (used directly in SupabaseRepository) ----
-keep class org.json.** { *; }

# ---- Compose / AndroidX ----
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class com.example.blankapp.ui.** { *; }

# ---- Enums (UserRole etc. used in serialization) ----
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ---- Parcelable / Serializable models ----
-keep class * implements android.os.Parcelable { *; }
-keep class * implements java.io.Serializable { *; }

# ---- Supabase config / BuildConfig fields consumed at runtime ----
-keep class com.example.blankapp.BuildConfig { *; }

# ---- General ----
-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
