package com.enumdev.enumrtp.commands;

import com.enumdev.enumrtp.Main;
import com.enumdev.enumrtp.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RTPReloadCommand implements CommandExecutor {
    private final Main plugin;

    public RTPReloadCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("enumrtp.reload") && !sender.isOp()) {
            sender.sendMessage(TextUtil.colorize("&cУ вас нет прав для выполнения этой команды."));
            return true;
        }
        // reload config safely
        plugin.getCfg().reload();
        String msg = plugin.getConfig().getString("messages.reload_success", "&aEnumRTP конфигурация перезагружена.");
        if (sender instanceof Player) {
            sender.sendMessage(TextUtil.parsePlaceholders((Player) sender, msg));
        } else {
            sender.sendMessage(TextUtil.colorize(msg));
        }
        return true;
    }
}
