# Add project specific ProGuard rules here.
# Release builds are not minified in Phase 1, so these rules are placeholders.

# Keep Room entity classes.
-keep class dev.ambitionsoftware.tymeboxed.data.db.entities.** { *; }

# Keep Gson TypeToken generic parameters.
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
