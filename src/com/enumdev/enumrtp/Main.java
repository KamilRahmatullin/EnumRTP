package com.enumdev.enumrtp;

import com.enumdev.enumrtp.commands.RTPCommand;
import com.enumdev.enumrtp.commands.RTPReloadCommand;
import com.enumdev.enumrtp.config.ConfigManager;
import com.enumdev.enumrtp.gui.MenuManager;
import com.enumdev.enumrtp.listeners.GUIListener;
import com.enumdev.enumrtp.managers.CooldownManager;
import com.enumdev.enumrtp.managers.TeleportManager;
import com.enumdev.enumrtp.utils.PlaceholderHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class Main extends JavaPlugin {
    private static Main instance;

    private ConfigManager configManager;
    private MenuManager menuManager;
    private TeleportManager teleportManager;
    private CooldownManager cooldownManager;
    private PlaceholderHook placeholderHook;

    private final List<Runnable> onDisableTasks = new ArrayList<>();

    public static Main getInstance() { return instance; }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        getLogger().log(Level.INFO, "Enabling EnumRTP v" + getDescription().getVersion());
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.cooldownManager = new CooldownManager();
        this.teleportManager = new TeleportManager(this, configManager, cooldownManager);
        this.menuManager = new MenuManager(this, configManager);
        this.placeholderHook = new PlaceholderHook(this);

        // Commands
        RTPCommand rtpCommand = new RTPCommand(this, menuManager, teleportManager);
        getCommand("rtp").setExecutor(rtpCommand);
        getCommand("rtpreload").setExecutor(new RTPReloadCommand(this));

        // Listeners
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new GUIListener(this, menuManager, teleportManager, cooldownManager), this);


        getLogger().log(Level.INFO, "EnumRTP enabled. Author: Liebert");
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "Disabling EnumRTP...");
        onDisableTasks.forEach(Runnable::run);
        // shutdown managers
        if (menuManager != null) menuManager.shutdown();
        if (teleportManager != null) teleportManager.shutdown();
        if (cooldownManager != null) cooldownManager.shutdown();
        getLogger().log(Level.INFO, "EnumRTP disabled.");
    }

    public void registerOnDisable(Runnable r) { onDisableTasks.add(r); }

    public ConfigManager getCfg() { return configManager; }
    public TeleportManager getTeleportManager() { return teleportManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public PlaceholderHook getPlaceholderHook() { return placeholderHook; }
}
