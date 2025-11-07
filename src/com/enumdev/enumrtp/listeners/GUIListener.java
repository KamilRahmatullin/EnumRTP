package com.enumdev.enumrtp.listeners;

import com.enumdev.enumrtp.Main;
import com.enumdev.enumrtp.config.ConfigManager;
import com.enumdev.enumrtp.managers.TeleportManager;
import com.enumdev.enumrtp.managers.CooldownManager;
import com.enumdev.enumrtp.utils.PDCUtils;
import com.enumdev.enumrtp.utils.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;

public class GUIListener implements Listener {
    private final Main plugin;
    private final ConfigManager cfg;
    private final TeleportManager teleportManager;
    private final CooldownManager cooldownManager;

    public GUIListener(Main plugin, com.enumdev.enumrtp.gui.MenuManager menuManager, TeleportManager teleportManager, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cfg = plugin.getCfg();
        this.teleportManager = teleportManager;
        this.cooldownManager = cooldownManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null) return;
        if (!clicked.hasItemMeta()) return;
        Player p = (Player) e.getWhoClicked();

        String back = PDCUtils.getString(clicked, "enumrtp_back_button");
        if (back != null) {
            e.setCancelled(true);
            p.closeInventory();
            ConfigurationSection items = cfg.getMenuItemsSection();
            if (items != null && items.isConfigurationSection("back")) {
                String action = items.getConfigurationSection("back").getString("action", "menu");
                    p.performCommand(action);
            }
            return;
        }

        String itemKey = PDCUtils.getString(clicked, "enumrtp_item");
        if (itemKey == null) return;
        e.setCancelled(true);
        ConfigurationSection items = cfg.getMenuItemsSection();
        if (items == null) return;
        ConfigurationSection section = items.getConfigurationSection(itemKey);
        if (section == null) return;
        String function = section.getString("function", "nothing");

        switch (function.toLowerCase()) {
            case "close":
                p.closeInventory();
                break;
            case "safe":
            case "far":
            case "player":
            case "base":
                p.closeInventory();
                teleportManager.requestTeleport(p, function.toLowerCase());
                break;
            default:
                break;
        }
    }
}
