# Room components
-keepnames class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.RoomDatabase { *; }
-keep class androidx.room.RoomDatabase$* { *; }

# Keep your Room Entities (adjust package if necessary)
-keep @androidx.room.Entity class * { *; }
-keep class com.dreamcode.task.** { *; }

# Biometric API
-keep class androidx.biometric.** { *; }
