package com.enumdev.enumrtp.commands;

import com.enumdev.enumrtp.Main;
import com.enumdev.enumrtp.gui.MenuManager;
import com.enumdev.enumrtp.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RTPCommand implements CommandExecutor {
    private final Main plugin;
    private final MenuManager menuManager;

    public RTPCommand(Main plugin, MenuManager menuManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextUtil.colorize("&cТолько игроки могут использовать эту команду."));
            return true;
        }
        Player p = (Player) sender;
        menuManager.openMenu(p);
        return true;
    }
}
