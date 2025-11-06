package com.enumdev.enumrtp.listeners;

import com.enumdev.enumrtp.managers.CooldownManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final CooldownManager cooldownManager;
    public PlayerQuitListener(CooldownManager cooldownManager) { this.cooldownManager = cooldownManager; }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cooldownManager.removePlayer(e.getPlayer().getUniqueId());
    }
}
