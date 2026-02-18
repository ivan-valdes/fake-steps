# ============================================================
# ProGuard / R8 rules for FakeSteps
# ============================================================

# --- Health Connect SDK ---
-keep class androidx.health.connect.client.** { *; }
-keep class androidx.health.platform.client.** { *; }

# --- Kotlin ---
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keep class kotlin.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }

# --- Jetpack Compose ---
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# --- WorkManager ---
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# --- DataStore ---
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# --- BroadcastReceivers (instantiated by the system) ---
-keep class com.fakesteps.receiver.AlarmReceiver { *; }
-keep class com.fakesteps.receiver.BootReceiver { *; }

# --- Application class ---
-keep class com.fakesteps.FakeStepsApp { *; }

# --- General Android ---
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Remove logging in release ---
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
