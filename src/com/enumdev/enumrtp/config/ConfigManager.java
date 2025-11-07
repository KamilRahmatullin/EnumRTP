package com.enumdev.enumrtp.config;

import com.enumdev.enumrtp.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ConfigManager {
    private final Main plugin;
    private FileConfiguration cfg;
    private File configFile;
    private boolean firstLoad = false;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!configFile.exists()) {
            firstLoad = true;
            plugin.saveResource("config.yml", false);
        }
        this.cfg = YamlConfiguration.loadConfiguration(configFile);
        ensureDefaults();
        if (!firstLoad && hasNewDefaults()) {
            saveConfigWithComments();
        }
    }

    private void ensureDefaults() {
        setDefault("menu.title", "&0&nРТП:");
        setDefault("menu.size", 54);

        setDefaultMenuItem("glass1", createGlass1Defaults());
        setDefaultMenuItem("back", createBackDefaults());
        setDefaultMenuItem("safe", createSafeDefaults());
        setDefaultMenuItem("far", createFarDefaults());
        setDefaultMenuItem("player", createPlayerDefaults());
        setDefaultMenuItem("base", createBaseDefaults());

        setDefault("main.safe.min-radius", 0);
        setDefault("main.safe.max-radius", 2500);
        setDefault("main.safe.allow-water-teleport", false);
        setDefault("main.safe.always-up", true);
        setDefault("main.safe.cooldown-seconds", 30);
        setDefault("main.safe.messages.successful_teleport", "&6РТП &8> &fВы успешно телепортированы на координаты &6{coordinates}&f.");
        setDefault("main.safe.messages.block_cooldown", "&6РТП &8> &fВы не можете телепортироваться ещё &6{seconds} сек.");
        setDefault("main.safe.messages.no_locations", "&6РТП &8> &cЛокация не найдена, попробуйте ещё раз!");
        setDefault("main.safe.messages.no_perm", "&6РТП &8> &cВы не можете телепортироваться на безопасную телепортацию!");

        setDefault("main.far.min-radius", 2000);
        setDefault("main.far.max-radius", 10000);
        setDefault("main.far.allow-water-teleport", false);
        setDefault("main.far.always-up", true);
        setDefault("main.far.cooldown-seconds", 60);
        setDefault("main.far.messages.successful_teleport", "&6РТП &8> &fВы успешно телепортированы на координаты &6{coordinates}&f.");
        setDefault("main.far.messages.block_cooldown", "&6РТП &8> &fВы не можете телепортироваться ещё &6{seconds} сек.");
        setDefault("main.far.messages.no_locations", "&6РТП &8> &cЛокация не найдена, попробуйте ещё раз!");
        setDefault("main.far.messages.no_perm", "&6РТП &8> &cВы не можете телепортироваться на далёкую телепортацию!");

        setDefault("main.player.min-radius", 50);
        setDefault("main.player.max-radius", 120);
        setDefault("main.player.allow-water-teleport", true);
        setDefault("main.player.always-up", true);
        setDefault("main.player.warnings", true);
        setDefault("main.player.warning-sound", "BLOCK_BARREL_OPEN");
        setDefault("main.player.min_online", 2);
        setDefault("main.player.cooldown-seconds", 300);
        setDefault("main.player.messages.successful_teleport", "&6РТП &8> &fВы успешно телепортированы на координаты &6{coordinates}&f рядом с игроком &6{player}&f.");
        setDefault("main.player.messages.block_cooldown", "&6РТП &8> &fВы не можете телепортироваться ещё &6{seconds} сек.");
        setDefault("main.player.messages.no_locations", "&6РТП &8> &cЛокация не найдена, попробуйте ещё раз!");
        setDefault("main.player.messages.no_perm", "&6РТП &8> &cВы не можете телепортироваться рядом с игроками!");
        setDefault("main.player.messages.warning", "&eВозле вас был телепортирован игрок, будьте осторожны!");

        setDefault("main.base.min-radius", 50);
        setDefault("main.base.max-radius", 150);
        setDefault("main.base.allow-water-teleport", true);
        setDefault("main.base.always-up", true);
        setDefault("main.base.cooldown-seconds", 600);
        setDefault("main.base.messages.successful_teleport", "&6РТП &8> &fВы успешно телепортированы на координаты &6{coordinates}&f рядом с регионом &6{region_name}&f.");
        setDefault("main.base.messages.block_cooldown", "&6РТП &8> &fВы не можете телепортироваться ещё &6{seconds} сек.");
        setDefault("main.base.messages.no_locations", "&6РТП &8> &cЛокация не найдена, попробуйте ещё раз!");
        setDefault("main.base.messages.no_perm", "&6РТП &8> &cВы не можете телепортироваться рядом с базами!");

        setDefault("settings.default-world", "world");
        setDefault("settings.disabled-worlds", Arrays.asList("spawn","pvp","duels"));

        setDefault("regions", Collections.emptyMap());
    }

    private void setDefault(String path, Object value) {
        if (!cfg.contains(path)) {
            cfg.set(path, value);
        }
    }

    private void setDefaultMenuItem(String itemName, Map<String, Object> defaults) {
        String basePath = "menu.items." + itemName + ".";
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            setDefault(basePath + entry.getKey(), entry.getValue());
        }
    }

    private Map<String, Object> createGlass1Defaults() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("display_name", "&0 ");
        defaults.put("description", Collections.emptyList());
        defaults.put("material", "BLACK_STAINED_GLASS_PANE");
        defaults.put("slots", Arrays.asList(0,8,45,53));
        return defaults;
    }

    private Map<String, Object> createBackDefaults() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("display_name", "&cПерейти назад ");
        defaults.put("description", Collections.emptyList());
        defaults.put("material", "ARROW");
        defaults.put("slots", Arrays.asList(49));
        defaults.put("function", "close");
        defaults.put("action", "menu");
        return defaults;
    }

    private Map<String, Object> createSafeDefaults() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("display_name", "&aБезопасный РТП");
        defaults.put("description", Arrays.asList("&7Телепортация в безопасное место.", "&7Радиус &60-2500 блоков"));
        defaults.put("material", "EMERALD");
        defaults.put("slots", Arrays.asList(20));
        defaults.put("function", "safe");
        return defaults;
    }

    private Map<String, Object> createFarDefaults() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("display_name", "&bДальний РТП");
        defaults.put("description", Arrays.asList("&7Телепортация на большие расстояния.", "&7Радиус &62000-10000 блоков"));
        defaults.put("material", "DIAMOND");
        defaults.put("slots", Arrays.asList(22));
        defaults.put("function", "far");
        return defaults;
    }

    private Map<String, Object> createPlayerDefaults() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("display_name", "&eИгрок РТП");
        defaults.put("description", Arrays.asList("&7Телепортация вблизи случайного игрока.", "&7Рискованно, но интересно!", "&7Радиус &650-120 блоков от игрока"));
        defaults.put("material", "PLAYER_HEAD");
        defaults.put("slots", Arrays.asList(24));
        defaults.put("function", "player");
        return defaults;
    }

    private Map<String, Object> createBaseDefaults() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("display_name", "&6База РТП");
        defaults.put("description", Arrays.asList("&7Телепортация рядом с базой.", "&7Радиус &650-150 блоков от региона"));
        defaults.put("material", "GOLD_INGOT");
        defaults.put("slots", Arrays.asList(40));
        defaults.put("function", "base");
        return defaults;
    }

    private boolean hasNewDefaults() {
        YamlConfiguration tempConfig = new YamlConfiguration();
        ensureDefaultsForCheck(tempConfig);

        for (String key : tempConfig.getKeys(true)) {
            if (!cfg.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private void ensureDefaultsForCheck(YamlConfiguration tempConfig) {
    }

    private void saveConfigWithComments() {
        try {
            File tempFile = new File(plugin.getDataFolder(), "config_temp.yml");
            cfg.save(tempFile);
            mergeConfigWithComments(tempFile, configFile);
            tempFile.delete();

        } catch (IOException e) {
            try {
                cfg.save(configFile);
            } catch (IOException ex) {
            }
        }
    }

    private void mergeConfigWithComments(File source, File destination) throws IOException {
        List<String> originalLines = new ArrayList<>();
        try (InputStream resourceStream = plugin.getResource("config.yml");
             BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                originalLines.add(line);
            }
        }

        List<String> currentLines = Files.readAllLines(source.toPath(), StandardCharsets.UTF_8);

        List<String> newLines = new ArrayList<>();
        newLines.addAll(originalLines);

        YamlConfiguration currentConfig = YamlConfiguration.loadConfiguration(source);
        for (String key : currentConfig.getKeys(true)) {
            if (!key.contains(".")) {
                boolean keyExists = originalLines.stream().anyMatch(line ->
                    line.trim().startsWith(key + ":") && !line.trim().startsWith("#"));

                if (!keyExists) {
                    Object value = currentConfig.get(key);
                    if (value instanceof List) {
                        newLines.add(key + ":");
                        for (Object item : (List<?>) value) {
                            newLines.add("  - " + item.toString());
                        }
                    } else {
                        newLines.add(key + ": " + value);
                    }
                }
            }
        }

        Files.write(destination.toPath(), newLines, StandardCharsets.UTF_8);
    }

    public void reload() {
        this.cfg = YamlConfiguration.loadConfiguration(configFile);
        ensureDefaults();

        if (hasNewDefaults()) {
            saveConfigWithComments();
        }
    }

    public FileConfiguration getConfig() {
        return cfg;
    }

    public ConfigurationSection getMenuSection() {
        return cfg.getConfigurationSection("menu");
    }

    public ConfigurationSection getMenuItemsSection() {
        return cfg.getConfigurationSection("menu.items");
    }
}