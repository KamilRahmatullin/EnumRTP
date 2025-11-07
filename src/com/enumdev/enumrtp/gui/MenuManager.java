package com.enumdev.enumrtp.gui;

import com.enumdev.enumrtp.Main;
import com.enumdev.enumrtp.config.ConfigManager;
import com.enumdev.enumrtp.utils.ItemUtil;
import com.enumdev.enumrtp.utils.PDCUtils;
import com.enumdev.enumrtp.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MenuManager {
    private final Main plugin;
    private final ConfigManager cfg;

    public MenuManager(Main plugin, ConfigManager cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
    }

    public void openMenu(Player p) {
        ConfigurationSection menu = cfg.getMenuSection();
        if (menu == null) return;
        String rawTitle = menu.getString("title", "&0РТП");
        int size = menu.getInt("size", 54);
        String title = TextUtil.colorize(rawTitle);
        Inventory inv = Bukkit.createInventory(null, size, title);
        ConfigurationSection items = cfg.getMenuItemsSection();
        if (items == null) return;
        for (String key : items.getKeys(false)) {
            ConfigurationSection item = items.getConfigurationSection(key);
            if (item == null) continue;
            ItemStack is = ItemUtil.itemStackFromSection(item, key);
            if (is == null) continue;
            for (int slot : item.getIntegerList("slots")) {
                if (slot < 0 || slot >= size) continue;
                ItemStack clone = is.clone();
                PDCUtils.setString(clone, "enumrtp_item", key);
                if (key.equalsIgnoreCase("back")) PDCUtils.setString(clone, "enumrtp_back_button", "true");
                inv.setItem(slot, clone);
            }
        }
        p.openInventory(inv);
    }

    public void shutdown() {
    }
}
