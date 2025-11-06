package com.enumdev.enumrtp.utils;

import com.enumdev.enumrtp.Main;
import org.bukkit.plugin.Plugin;

public class PlaceholderHook {
    private final Main plugin;
    private boolean hooked = false;
    public PlaceholderHook(Main plugin) {
        this.plugin = plugin;
        Plugin p = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (p != null) hooked = true;
    }
    public boolean isHooked() { return hooked; }
}
