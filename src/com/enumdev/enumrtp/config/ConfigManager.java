package com.enumdev.enumrtp.config;

import com.enumdev.enumrtp.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ConfigManager {
    private final Main plugin;
    private FileConfiguration cfg;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        this.cfg = plugin.getConfig();
        ensureDefaults();
        plugin.saveConfig();
    }

    private void ensureDefaults() {
        // menu defaults
        cfg.addDefault("menu.title", "&0&nРТП:");
        cfg.addDefault("menu.size", 54);
        Map<String, Object> items = new LinkedHashMap<>();
        Map<String, Object> glass1 = new LinkedHashMap<>();
        glass1.put("display_name", "&0 ");
        glass1.put("description", Collections.emptyList());
        glass1.put("material", "BLACK_STAINED_GLASS_PANE");
        glass1.put("slots", Arrays.asList(0,8,45,53));
        items.put("glass1", glass1);
        Map<String, Object> back = new LinkedHashMap<>();
        back.put("display_name", "&cПерейти назад ");
        back.put("description", Collections.emptyList());
        back.put("material", "ARROW");
        back.put("slots", Arrays.asList(49));
        back.put("function", "close");
        back.put("action", "menu");
        items.put("back", back);
        Map<String, Object> safe = new LinkedHashMap<>();
        safe.put("display_name", "&aБезопасный РТП");
        safe.put("description", Arrays.asList("&7Телепортация в безопасное место.", "&7Радиус &60-2500 блоков"));
        safe.put("material", "EMERALD");
        safe.put("slots", Arrays.asList(20));
        safe.put("function", "safe");
        items.put("safe", safe);
        Map<String, Object> far = new LinkedHashMap<>();
        far.put("display_name", "&bДальний РТП");
        far.put("description", Arrays.asList("&7Телепортация на большие расстояния.", "&7Радиус &62000-10000 блоков"));
        far.put("material", "DIAMOND");
        far.put("slots", Arrays.asList(22));
        far.put("function", "far");
        items.put("far", far);
        Map<String, Object> player = new LinkedHashMap<>();
        player.put("display_name", "&eИгрок РТП");
        player.put("description", Arrays.asList("&7Телепортация вблизи случайного игрока.", "&7Рискованно, но интересно!", "&7Радиус &650-120 блоков от игрока"));
        player.put("material", "PLAYER_HEAD");
        player.put("slots", Arrays.asList(24));
        player.put("function", "player");
        items.put("player", player);
        Map<String, Object> base = new LinkedHashMap<>();
        base.put("display_name", "&6База РТП");
        base.put("description", Arrays.asList("&7Телепортация рядом с базой.", "&7Радиус &650-150 блоков от региона"));
        base.put("material", "GOLD_INGOT");
        base.put("slots", Arrays.asList(40));
        base.put("function", "base");
        items.put("base", base);
        cfg.addDefault("menu.items", items);

        // main defaults
        cfg.addDefault("main.safe.min-radius", 0);
        cfg.addDefault("main.safe.max-radius", 2500);
        cfg.addDefault("main.safe.allow-water-teleport", false);
        cfg.addDefault("main.safe.always-up", true);
        cfg.addDefault("main.safe.cooldown-seconds", 30);
        cfg.addDefault("main.safe.messages.successful_teleport", "&6РТП &8> &fВы успешно телепортированы на координаты &6{coordinates}&f.");
        cfg.addDefault("main.safe.messages.block_cooldown", "&6РТП &8> &fВы не можете телепортироваться ещё &6{seconds} сек.");
        cfg.addDefault("main.safe.messages.no_locations", "&6РТП &8> &cЛокация не найдена, попробуйте ещё раз!");
        cfg.addDefault("main.safe.messages.no_perm", "&6РТП &8> &cВы не можете телепортироваться на безопасную телепортацию!");

        cfg.addDefault("main.far.min-radius", 2000);
        cfg.addDefault("main.far.max-radius", 10000);
        cfg.addDefault("main.far.allow-water-teleport", false);
        cfg.addDefault("main.far.always-up", true);
        cfg.addDefault("main.far.cooldown-seconds", 60);
        cfg.addDefault("main.far.messages.successful_teleport", "&6РТП &8> &fВы успешно телепортированы на координаты &6{coordinates}&f.");
        cfg.addDefault("main.far.messages.block_cooldown", "&6РТП &8> &fВы не можете телепортироваться ещё &6{seconds} сек.");
        cfg.addDefault("main.far.messages.no_locations", "&6РТП &8> &cЛокация не найдена, попробуйте ещё раз!");
        cfg.addDefault("main.far.messages.no_perm", "&6РТП &8> &cВы не можете телепортироваться на далёкую телепортацию!");

        cfg.addDefault("main.player.min-radius", 50);
        cfg.addDefault("main.player.max-radius", 120);
        cfg.addDefault("main.player.allow-water-teleport", true);
        cfg.addDefault("main.player.always-up", true);
        cfg.addDefault("main.player.warnings", true);
        cfg.addDefault("main.player.min_online", 2);
        cfg.addDefault("main.player.cooldown-seconds", 300);
        cfg.addDefault("main.player.messages.successful_teleport", "&6РТП &8> &fВы успешно телепортированы на координаты &6{coordinates}&f рядом с игроком &6{player}&f.");
        cfg.addDefault("main.player.messages.block_cooldown", "&6РТП &8> &fВы не можете телепортироваться ещё &6{seconds} сек.");
        cfg.addDefault("main.player.messages.no_locations", "&6РТП &8> &cЛокация не найдена, попробуйте ещё раз!");
        cfg.addDefault("main.player.messages.no_perm", "&6РТП &8> &cВы не можете телепортироваться рядом с игроками!");
        cfg.addDefault("main.player.messages.warning", "&eВозле вас был телепортирован игрок, будьте осторожны!");

        cfg.addDefault("main.base.min-radius", 50);
        cfg.addDefault("main.base.max-radius", 150);
        cfg.addDefault("main.base.allow-water-teleport", true);
        cfg.addDefault("main.base.always-up", true);
        cfg.addDefault("main.base.cooldown-seconds", 600);
        cfg.addDefault("main.base.messages.successful_teleport", "&6РТП &8> &fВы успешно телепортированы на координаты &6{coordinates}&f рядом с регионом &6{region_name}&f.");
        cfg.addDefault("main.base.messages.block_cooldown", "&6РТП &8> &fВы не можете телепортироваться ещё &6{seconds} сек.");
        cfg.addDefault("main.base.messages.no_locations", "&6РТП &8> &cЛокация не найдена, попробуйте ещё раз!");
        cfg.addDefault("main.base.messages.no_perm", "&6РТП &8> &cВы не можете телепортироваться рядом с базами!");

        cfg.addDefault("settings.default-world", "world");
        cfg.addDefault("settings.disabled-worlds", Arrays.asList("spawn","pvp","duels"));

        cfg.addDefault("regions", Collections.emptyMap());

        cfg.options().copyDefaults(true);
    }

    public void reload() {
        plugin.reloadConfig();
        this.cfg = plugin.getConfig();
        cfg.options().copyDefaults(true);
        ensureDefaults();
        plugin.saveConfig();
    }

    public FileConfiguration getConfig() { return cfg; }

    public ConfigurationSection getMenuSection() { return cfg.getConfigurationSection("menu"); }
    public ConfigurationSection getMenuItemsSection() { return cfg.getConfigurationSection("menu.items"); }
}