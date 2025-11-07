package com.enumdev.enumrtp.commands;

import com.enumdev.enumrtp.Main;
import com.enumdev.enumrtp.gui.MenuManager;
import com.enumdev.enumrtp.managers.TeleportManager;
import com.enumdev.enumrtp.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class RTPCommand implements CommandExecutor {
    private final Main plugin;
    private final MenuManager menuManager;
    private final TeleportManager teleportManager;

    public RTPCommand(Main plugin, MenuManager menuManager, TeleportManager teleportManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.teleportManager = teleportManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextUtil.colorize("&cТолько игроки могут использовать эту команду."));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            menuManager.openMenu(player);
            return true;
        }

        String input = args[0].toLowerCase();

        ConfigurationSection mainSection = plugin.getCfg().getConfig().getConfigurationSection("main");
        if (mainSection != null) {
            for (String teleportType : mainSection.getKeys(false)) {
                ConfigurationSection typeSection = mainSection.getConfigurationSection(teleportType);
                if (typeSection != null) {
                    List<String> aliases = typeSection.getStringList("aliases");

                    if (aliases.contains(input)) {
                        teleportManager.requestTeleport(player, teleportType);
                        return true;
                    }
                }
            }
        }

        menuManager.openMenu(player);
        return true;
    }
}