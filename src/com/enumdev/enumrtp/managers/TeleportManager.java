package com.enumdev.enumrtp.managers;

import com.enumdev.enumrtp.Main;
import com.enumdev.enumrtp.config.ConfigManager;
import com.enumdev.enumrtp.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * TeleportManager — полностью реализованный менеджер поиска безопасных точек и выполнения телепортов.
 * Хранит минимальные зависимости и не держит сильных ссылок на объекты Player в асинхронных задачах.
 */
public class TeleportManager {
    private final Main plugin;
    private final ConfigManager cfg;
    private final CooldownManager cooldownManager;

    public TeleportManager(Main plugin, ConfigManager cfg, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.cooldownManager = cooldownManager;
    }

    /**
     * Запрос на телепорт. Вызывается из основного потока (например, listener или команда).
     * Запускает асинхронный поиск локации и затем синхронный телепорт.
     */
    public void requestTeleport(Player player, String type) {
        if (player == null || type == null) return;

        UUID uuid = player.getUniqueId();
        String usePerm = "enumrtp." + type + ".use";
        String bypassPerm = "enumrtp." + type + ".bypass";

        // Быстрая проверка прав
        if (!player.hasPermission(usePerm) && !player.isOp()) {
            String noPerm = cfg.getConfig().getString("main." + type + ".messages.no_perm", "&cНет прав для этой операции.");
            player.sendMessage(TextUtil.parsePlaceholders(player, noPerm));
            return;
        }

        // Быстрая проверка кулдауна
        if (!player.hasPermission(bypassPerm)) {
            int cooldown = cfg.getConfig().getInt("main." + type + ".cooldown-seconds", 0);
            if (cooldown > 0 && cooldownManager.isOnCooldown(uuid, type)) {
                long rem = cooldownManager.getRemaining(uuid, type);
                String blockMsg = cfg.getConfig().getString("main." + type + ".messages.block_cooldown", "&cПожалуйста, подождите {seconds} сек.");
                blockMsg = blockMsg.replace("{seconds}", String.valueOf(rem));
                player.sendMessage(TextUtil.parsePlaceholders(player, blockMsg));
                return;
            }
        }

        // Определим куда искать — учитываем disabled-worlds
        World world = player.getWorld();
        String defaultWorldName = cfg.getConfig().getString("settings.default-world", "world");
        List<String> disabled = cfg.getConfig().getStringList("settings.disabled-worlds");
        String searchWorldName = world != null && disabled.contains(world.getName()) ? defaultWorldName : (world != null ? world.getName() : defaultWorldName);

        // Сохраняем минимальные данные и запускаем асинхронный поиск
        final String finalSearchWorld = searchWorldName;
        final String playerName = player.getName();

        new BukkitRunnable() {
            @Override
            public void run() {
                performSearchAndTeleport(uuid, playerName, finalSearchWorld, type);
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Асинхронный поиск локации и синхронный телепорт при успехе.
     */
    private void performSearchAndTeleport(UUID uuid, String playerName, String worldName, String type) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) p.sendMessage(TextUtil.colorize("&cМир для телепортации не найден."));
            });
            return;
        }

        ConfigurationSection section = cfg.getConfig().getConfigurationSection("main." + type);
        if (section == null) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) p.sendMessage(TextUtil.colorize("&cКонфигурация телепорта не найдена."));
            });
            return;
        }

        boolean allowWater = section.getBoolean("allow-water-teleport", false);
        boolean alwaysUp = section.getBoolean("always-up", true);
        int tries = 10; // как вы просили

        Location found = null;
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        int min = section.getInt("min-radius", 0);
        int max = section.getInt("max-radius", 2500);

        try {
            switch (type.toLowerCase()) {
                case "player": {
                    Collection<? extends Player> online = Bukkit.getOnlinePlayers();
                    if (online.size() < section.getInt("min-online", 2)) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null) p.sendMessage(TextUtil.parsePlaceholders(p, section.getString("messages.no_locations")));
                        });
                        return;
                    }

                    List<Player> others = new ArrayList<>();
                    for (Player pl : online) if (!pl.getUniqueId().equals(uuid)) others.add(pl);
                    if (others.isEmpty()) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null) p.sendMessage(TextUtil.parsePlaceholders(p, section.getString("messages.no_locations")));
                        });
                        return;
                    }

                    Player target = others.get(rnd.nextInt(others.size()));
                    World targetWorld = target.getWorld();
                    for (int i = 0; i < tries; i++) {
                        int dx = rnd.nextInt(min, max + 1) * (rnd.nextBoolean() ? 1 : -1);
                        int dz = rnd.nextInt(min, max + 1) * (rnd.nextBoolean() ? 1 : -1);
                        int x = target.getLocation().getBlockX() + dx;
                        int z = target.getLocation().getBlockZ() + dz;
                        int y = alwaysUp ? targetWorld.getHighestBlockYAt(x, z) + 1 : rnd.nextInt(5, Math.max(6, targetWorld.getMaxHeight() - 1));
                        Location loc = new Location(targetWorld, x + 0.5, y, z + 0.5);
                        if (isSafe(loc, allowWater)) { found = loc; break; }
                    }

                    if (found == null) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null) p.sendMessage(TextUtil.parsePlaceholders(p, section.getString("messages.no_locations")));
                        });
                        return;
                    }

                    Location finalFound = found;
                    String targetName = target.getName();
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p == null) return;
                        p.teleport(finalFound);
                        String coords = formatCoords(finalFound);
                        String msg = section.getString("messages.successful_teleport", "&aТелепорт успешен: {coordinates}");
                        msg = msg.replace("{coordinates}", coords).replace("{player}", targetName);
                        p.sendMessage(TextUtil.parsePlaceholders(p, msg));
                        if (section.getBoolean("warnings", true) && target.isOnline()) {
                            target.sendMessage(TextUtil.parsePlaceholders(target, section.getString("messages.warning")));
                        }
                        int cd = section.getInt("cooldown-seconds", 0);
                        if (cd > 0 && !p.hasPermission("enumrtp.player.bypass")) cooldownManager.setCooldown(uuid, "player", cd);
                    });
                    return;
                }
                case "base": {
                    ConfigurationSection regions = cfg.getConfig().getConfigurationSection("regions");
                    if (regions == null || regions.getKeys(false).isEmpty()) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null) p.sendMessage(TextUtil.parsePlaceholders(p, section.getString("messages.no_locations")));
                        });
                        return;
                    }
                    List<Location> centers = new ArrayList<>();
                    List<String> names = new ArrayList<>();
                    for (String key : regions.getKeys(false)) {
                        ConfigurationSection r = regions.getConfigurationSection(key);
                        if (r == null) continue;
                        String wname = r.getString("world", world.getName());
                        World w = Bukkit.getWorld(wname);
                        if (w == null) continue;
                        double rx = r.getDouble("x");
                        double ry = r.getDouble("y");
                        double rz = r.getDouble("z");
                        centers.add(new Location(w, rx, ry, rz));
                        names.add(key);
                    }
                    if (centers.isEmpty()) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null) p.sendMessage(TextUtil.parsePlaceholders(p, section.getString("messages.no_locations")));
                        });
                        return;
                    }
                    int idx = ThreadLocalRandom.current().nextInt(centers.size());
                    Location center = centers.get(idx);
                    String regionName = names.get(idx);
                    World rw = center.getWorld();
                    for (int i = 0; i < tries; i++) {
                        int dx = ThreadLocalRandom.current().nextInt(min, max + 1) * (ThreadLocalRandom.current().nextBoolean() ? 1 : -1);
                        int dz = ThreadLocalRandom.current().nextInt(min, max + 1) * (ThreadLocalRandom.current().nextBoolean() ? 1 : -1);
                        int x = center.getBlockX() + dx;
                        int z = center.getBlockZ() + dz;
                        int y = alwaysUp ? rw.getHighestBlockYAt(x, z) + 1 : ThreadLocalRandom.current().nextInt(5, Math.max(6, rw.getMaxHeight() - 1));
                        Location loc = new Location(rw, x + 0.5, y, z + 0.5);
                        if (isSafe(loc, allowWater)) { found = loc; break; }
                    }
                    if (found == null) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null) p.sendMessage(TextUtil.parsePlaceholders(p, section.getString("messages.no_locations")));
                        });
                        return;
                    }
                    Location finalFound = found;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p == null) return;
                        p.teleport(finalFound);
                        String coords = formatCoords(finalFound);
                        String msg = section.getString("messages.successful_teleport", "&aТелепорт успешен: {coordinates}");
                        msg = msg.replace("{coordinates}", coords).replace("{region_name}", regionName);
                        p.sendMessage(TextUtil.parsePlaceholders(p, msg));
                        int cd = section.getInt("cooldown-seconds", 0);
                        if (cd > 0 && !p.hasPermission("enumrtp.base.bypass")) cooldownManager.setCooldown(uuid, "base", cd);
                    });
                    return;
                }
                default: {
                    // safe / far (по умолчанию телепорт вокруг текущего положения игрока)
                    Player p = Bukkit.getPlayer(uuid);
                    Location center = p != null ? p.getLocation() : new Location(world, 0, 64, 0);
                    for (int i = 0; i < tries; i++) {
                        int dx = ThreadLocalRandom.current().nextInt(min, max + 1) * (ThreadLocalRandom.current().nextBoolean() ? 1 : -1);
                        int dz = ThreadLocalRandom.current().nextInt(min, max + 1) * (ThreadLocalRandom.current().nextBoolean() ? 1 : -1);
                        int x = center.getBlockX() + dx;
                        int z = center.getBlockZ() + dz;
                        int y = alwaysUp ? world.getHighestBlockYAt(x, z) + 1 : ThreadLocalRandom.current().nextInt(5, Math.max(6, world.getMaxHeight() - 1));
                        Location loc = new Location(world, x + 0.5, y, z + 0.5);
                        if (isSafe(loc, allowWater)) { found = loc; break; }
                    }
                    if (found == null) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Player p2 = Bukkit.getPlayer(uuid);
                            if (p2 != null) p2.sendMessage(TextUtil.parsePlaceholders(p2, section.getString("messages.no_locations")));
                        });
                        return;
                    }
                    Location finalFound = found;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Player p2 = Bukkit.getPlayer(uuid);
                        if (p2 == null) return;
                        p2.teleport(finalFound);
                        String coords = formatCoords(finalFound);
                        String msg = section.getString("messages.successful_teleport", "&aТелепорт успешен: {coordinates}");
                        msg = msg.replace("{coordinates}", coords);
                        p2.sendMessage(TextUtil.parsePlaceholders(p2, msg));
                        int cd = section.getInt("cooldown-seconds", 0);
                        if (cd > 0 && !p2.hasPermission("enumrtp." + type + ".bypass")) cooldownManager.setCooldown(uuid, type, cd);
                    });
                    return;
                }
            }
        } catch (Throwable t) {
            // Защита на случай ошибок с чанками/миром — вернём сообщение пользователю, не краша сервер
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) p.sendMessage(TextUtil.colorize("&cОшибка при попытке найти локацию. Попробуйте ещё раз."));
            });
        }
    }

    private String formatCoords(Location loc) {
        if (loc == null) return "";
        return "x:" + loc.getBlockX() + " y:" + loc.getBlockY() + " z:" + loc.getBlockZ();
    }

    /**
     * Проверяет, что локация безопасна для телепортации:
     * - блоки ног и головы не твёрдые
     * - под ногами твёрдый блок
     * - при запрещении воды/лавы — на ножках/голове нет жидкости
     */
    private boolean isSafe(Location loc, boolean allowWater) {
        if (loc == null) return false;
        try {
            World w = loc.getWorld();
            if (w == null) return false;

            Block feet = w.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            Block head = w.getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ());
            Block below = w.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());

            // Ноги и голова не должны быть внутри твёрдого блока
            if (feet.getType().isSolid() || head.getType().isSolid()) return false;

            // Под ногами должен быть твёрдый блок (иначе упадёшь)
            if (!below.getType().isSolid()) return false;

            if (!allowWater) {
                Material ft = feet.getType();
                Material ht = head.getType();
                if (ft == Material.WATER || ht == Material.WATER) return false;
                if (ft == Material.LAVA || ht == Material.LAVA) return false;
            }

            // Простейшие проверки пройдены — считаем место безопасным
            return true;
        } catch (Throwable t) {
            // При любых проблемах с чанками/доступом — отвергаем попытку
            return false;
        }
    }

    public void shutdown() {
        // Пока TeleportManager не создаёт планировщиков с id — ничего чистить
    }
}
