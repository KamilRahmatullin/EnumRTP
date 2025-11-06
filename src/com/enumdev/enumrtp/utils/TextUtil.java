package com.enumdev.enumrtp.utils;

import com.enumdev.enumrtp.Main;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class TextUtil {
    public static String colorize(String s) {
        if (s == null) return "";
        s = translateHexCodes(s);
        s = ChatColor.translateAlternateColorCodes('&', s);
        return s;
    }

    public static String parsePlaceholders(Player p, String s) {
        if (s == null) return "";
        s = colorize(s);
        if (p != null && Main.getInstance() != null && Main.getInstance().getPlaceholderHook() != null && Main.getInstance().getPlaceholderHook().isHooked()) {
            try {
                return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, s);
            } catch (Throwable ignored) {
                return s;
            }
        }
        return s;
    }

    private static String translateHexCodes(String message) {
        try {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(&?#)([A-Fa-f0-9]{6})").matcher(message);
            StringBuffer buffer = new StringBuffer();
            while (matcher.find()) {
                String hex = matcher.group(2);
                StringBuilder replacement = new StringBuilder();
                replacement.append('\u00A7').append('x');
                for (char c : hex.toCharArray()) {
                    replacement.append('\u00A7').append(c);
                }
                matcher.appendReplacement(buffer, replacement.toString());
            }
            matcher.appendTail(buffer);
            return buffer.toString();
        } catch (Throwable t) {
            return message;
        }
    }
}
