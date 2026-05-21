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

# --- Project-specific ProGuard Rules for Kitab al-Huda ---

# Keep all classes and their members in your data models, entities, and network packages
-keep class com.alfred.kitabalhuda.model.** { *; }
-keep class com.alfred.kitabalhuda.entity.** { *; }
-keep class com.alfred.kitabalhuda.network.** { *; }
-keep class com.alfred.kitabalhuda.repository.** { *; }
-keep class com.alfred.kitabalhuda.database.** { *; }
-keep class com.alfred.kitabalhuda.worker.** { *; }
-keep class com.alfred.kitabalhuda.utils.** { *; }

# Keep fields of data model classes for serialization/deserialization (e.g., with Gson)
-keepclassmembers class com.alfred.kitabalhuda.model.** {
  <fields>;
}
-keepclassmembers class com.alfred.kitabalhuda.entity.** {
  <fields>;
}
-keepclassmembers class com.alfred.kitabalhuda.network.** {
  <fields>;
}

# Keep all classes and their members in your API interfaces package (Retrofit)
-keep interface com.alfred.kitabalhuda.network.** { *; }

# Room Persistence Library rules
# Keep Room entities, DAOs, and database classes
-keep class com.alfred.kitabalhuda.database.AppDatabase { *; }
-keep class com.alfred.kitabalhuda.database.dao.** { *; }
-keep class com.alfred.kitabalhuda.database.Converters { *; }
-keep class com.alfred.kitabalhuda.entity.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.Entity { *; }
-keep class * extends androidx.room.Dao { *; }
-keep class * extends androidx.room.TypeConverter { *; }
-keep class * extends androidx.room.PrimaryKey { *; }
-keep class * extends androidx.room.ColumnInfo { *; }
-keep class * extends androidx.room.Ignore { *; }
-keep class * extends androidx.room.Embedded { *; }
-keep class * extends androidx.room.Relation { *; }
-keep class * extends androidx.room.Query { *; }
-keep class * extends androidx.room.Insert { *; }
-keep class * extends androidx.room.Update { *; }
-keep class * extends androidx.room.Delete { *; }
-keep class * extends androidx.room.Transaction { *; }
-keep class * extends androidx.room.RawQuery { *; }
-keep class * extends androidx.room.Fts4 { *; }
-keep class * extends androidx.room.Fts3 { *; }
-keep class * extends androidx.room.Index { *; }
-keep class * extends androidx.room.ForeignKey { *; }
-keep class * extends androidx.room.Junction { *; }
-keep class * extends androidx.room.MapInfo { *; }
-keep class * extends androidx.room.ProvidedTypeConverter { *; }
-keep class * extends androidx.room.DatabaseView { *; }

# Gson rules (often handled by converter-gson, but explicit rules can help)
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class * implements com.google.gson.InstanceCreator
-keep class com.google.gson.internal.UnsafeAllocator { *; }
-keep class com.google.gson.internal.bind.TreeTypeAdapter$SingleTypeFactory { *; }

# WorkManager rules
-keep class com.alfred.kitabalhuda.worker.** { *; }
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }

# Glide rules
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl
-keep class com.bumptech.glide.Glide { *; }
-keep class com.bumptech.glide.load.resource.bitmap.HardwareConfigState { *; }

# ExoPlayer rules
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.ui.** { *; }
-keep class androidx.media3.exoplayer.dash.** { *; }
-keep class com.google.android.exoplayer2.** { *; }
-keep class com.google.android.exoplayer2.ui.** { *; }

# Keep enums
-keepclassmembers enum * {
  *;
}

# Keep BuildConfig fields
-keep class **.BuildConfig {
  public static final boolean DEBUG;
  public static final java.lang.String APPLICATION_ID;
  public static final java.lang.String BUILD_TYPE;
  public static final int VERSION_CODE;
  public static final java.lang.String VERSION_NAME;
  public static final java.lang.String ENCRYPTION_KEY_PART1;
  public static final java.lang.String ENCRYPTED_FACEBOOK_BASE_URL;
  public static final java.lang.String ENCRYPTED_FACEBOOK_PAGE_ID;
  public static final java.lang.String ENCRYPTED_FACEBOOK_ACCESS_TOKEN;
}

# --- UI-related ProGuard Rules for Kitab al-Huda ---

# Keep all UI-related classes (Activities, Fragments, ViewModels, Adapters)
-keep class com.alfred.kitabalhuda.ui.** { *; }

# Keep ViewModelFactory for proper ViewModel instantiation
-keep class com.alfred.kitabalhuda.di.ViewModelFactory { *; }

# Keep all classes that extend Activity, Fragment, ViewModel, etc.
-keep public class * extends android.app.Activity
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends androidx.lifecycle.ViewModel
-keep public class * extends androidx.recyclerview.widget.RecyclerView.Adapter
-keep public class * extends androidx.viewpager.widget.PagerAdapter
-keep public class * extends android.app.Application

# Keep custom views if any
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep Data Binding generated classes
-keep class **.databinding.** { *; }
-keep class com.alfred.kitabalhuda.BR { *; } # If you use BR class for data binding

# Keep annotations for Data Binding
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes *Annotation*

# Keep all fields and methods of classes that are accessed via reflection
# This is a generic rule, be more specific if possible
-keep class * {
    <init>(...);
    <fields>;
    <methods>;
}

# Generated by R8 to suppress warnings about missing classes
-dontwarn java.lang.reflect.AnnotatedType