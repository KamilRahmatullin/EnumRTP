package com.enumdev.enumrtp.utils;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemUtil {
    public static ItemStack itemStackFromSection(ConfigurationSection section, String key) {
        try {
            String matName = section.getString("material", "STONE");
            Material mat = Material.getMaterial(matName.toUpperCase());
            if (mat == null) mat = Material.STONE;
            ItemStack is = new ItemStack(mat);
            ItemMeta meta = is.getItemMeta();
            if (meta == null) return is;
            String dn = section.getString("display_name", "");
            meta.setDisplayName(TextUtil.colorize(dn));
            List<String> lore = new ArrayList<>();
            for (String l : section.getStringList("description")) lore.add(TextUtil.colorize(l));
            meta.setLore(lore);
            is.setItemMeta(meta);
            return is;
        } catch (Exception e) {
            return null;
        }
    }
}
