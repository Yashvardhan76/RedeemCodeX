# Preserve the Spigot plugin main class and plugin.yml entries
-keep public class me.justlime.redeemX.RedeemX {
    public <methods>;
}


# Keep annotations
-keepattributes *Annotation*

# Preserve necessary Kotlin classes
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    *;
}

# Retain reflection-based members (e.g., Bukkit/Spigot listeners, commands)
-keepclassmembers class * {
    @org.bukkit.event.* <methods>;
    public void on*Event(...);
}

# Exclude all warning messages
-dontwarn kotlin.**
-dontwarn javax.**

# Optimize bytecode
-optimizations !code/simplification/arithmetic
