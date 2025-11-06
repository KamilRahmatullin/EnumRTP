package com.enumdev.enumrtp.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;

public class PDCUtils {
    private static final JavaPlugin plugin = com.enumdev.enumrtp.Main.getInstance();

    public static void setString(ItemStack is, String key, String value) {
        if (is == null) return;
        if (!is.hasItemMeta()) return;
        ItemMeta meta = is.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey(plugin, key), PersistentDataType.STRING, value);
        is.setItemMeta(meta);
    }

    public static String getString(ItemStack is, String key) {
        if (is == null) return null;
        if (!is.hasItemMeta()) return null;
        ItemMeta meta = is.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey nk = new NamespacedKey(plugin, key);
        return pdc.has(nk, PersistentDataType.STRING) ? pdc.get(nk, PersistentDataType.STRING) : null;
    }
}
