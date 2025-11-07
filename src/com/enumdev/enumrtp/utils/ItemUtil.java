package com.enumdev.enumrtp.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
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
            Bukkit.getLogger().info(dn);

            int modelData = section.getInt("model_data", -1);
            if (modelData != -1) {
                meta.setCustomModelData(modelData);
            }
            List<String> enchantments = section.getStringList("enchantments");
            for (String enchantStr : enchantments) {
                String[] parts = enchantStr.split(";");
                if (parts.length == 2) {
                    Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(parts[0].toLowerCase()));
                    if (enchant != null) {
                        int level = Integer.parseInt(parts[1]);
                        meta.addEnchant(enchant, level, true);
                    }
                }
            }

            if (section.getBoolean("hide_enchantments", false)) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            if (section.getBoolean("hide_effects", false)) {
                meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            }
            if (section.getBoolean("hide_attributes", false)) {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }

            is.setItemMeta(meta);
            return is;
        } catch (Exception e) {
            return null;
        }
    }
}
