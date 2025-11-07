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

public class TeleportManager {
    private final Main plugin;
    private final ConfigManager cfg;
    private final CooldownManager cooldownManager;

    public TeleportManager(Main plugin, ConfigManager cfg, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.cooldownManager = cooldownManager;
    }

    public void requestTeleport(Player player, String type) {
        if (player == null || type == null) return;
        UUID uuid = player.getUniqueId();
        String usePerm = "enumrtp." + type + ".use";
        String bypassPerm = "enumrtp." + type + ".bypass";
        if (!player.hasPermission(usePerm) && !player.isOp()) {
            String noPerm = cfg.getConfig().getString("main." + type + ".messages.no_perm", "&cНет прав для этой операции.");
            player.sendMessage(TextUtil.parsePlaceholders(player, noPerm));
            return;
        }
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
        World world = player.getWorld();
        String defaultWorldName = cfg.getConfig().getString("settings.default-world", "world");
        List<String> disabled = cfg.getConfig().getStringList("settings.disabled-worlds");
        String searchWorldName = world != null && disabled.contains(world.getName()) ? defaultWorldName : (world != null ? world.getName() : defaultWorldName);
        final String finalSearchWorld = searchWorldName;
        final String playerName = player.getName();
        new BukkitRunnable() {
            @Override
            public void run() {
                performSearchAndTeleport(player.getUniqueId(), playerName, finalSearchWorld, type);
            }
        }.runTaskAsynchronously(plugin);
    }

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
        int tries = 10;
        Location found = null;
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int min = section.getInt("min-radius", 0);
        int max = section.getInt("max-radius", 2500);

        try {
            switch (type.toLowerCase()) {
                case "player": {
                    Player executor = Bukkit.getPlayer(uuid);
                    String defaultWorldName = cfg.getConfig().getString("settings.default-world", "world");
                    List<String> disabledWorlds = cfg.getConfig().getStringList("settings.disabled-worlds");

                    String searchWorldName;
                    if (executor != null) {
                        World execWorld = executor.getWorld();
                        if (execWorld != null && disabledWorlds.contains(execWorld.getName())) {
                            searchWorldName = defaultWorldName;
                        } else {
                            searchWorldName = execWorld != null ? execWorld.getName() : defaultWorldName;
                        }
                    } else {
                        searchWorldName = world != null ? world.getName() : defaultWorldName;
                    }

                    World playersWorld = Bukkit.getWorld(searchWorldName);
                    if (playersWorld == null) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null)
                                p.sendMessage(TextUtil.parsePlaceholders(p, section.getString("messages.no_locations")));
                        });
                        return;
                    }

                    int minOnlineNeeded = section.getInt("min-online", 2);
                    List<Player> others = new ArrayList<>();
                    int playersInSameWorld = 0;
                    for (Player pl : Bukkit.getOnlinePlayers()) {
                        if (!pl.getWorld().equals(playersWorld)) continue;
                        playersInSameWorld++;
                        if (!pl.getUniqueId().equals(uuid)) others.add(pl);
                    }

                    if (playersInSameWorld < minOnlineNeeded) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null)
                                p.sendMessage(TextUtil.parsePlaceholders(p, section.getString("messages.no_locations")));
                        });
                        return;
                    }

                    if (others.isEmpty()) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null)
                                p.sendMessage(TextUtil.parsePlaceholders(p, section.getString("messages.no_locations")));
                        });
                        return;
                    }

                    Player target = others.get(rnd.nextInt(others.size()));
                    World targetWorld = target.getWorld();
                    int tmin = section.getInt("min-radius", 50);
                    int tmax = section.getInt("max-radius", 120);

                    for (int i = 0; i < tries; i++) {
                        int dx = ThreadLocalRandom.current().nextInt(tmin, tmax + 1) * (ThreadLocalRandom.current().nextBoolean() ? 1 : -1);
                        int dz = ThreadLocalRandom.current().nextInt(tmin, tmax + 1) * (ThreadLocalRandom.current().nextBoolean() ? 1 : -1);
                        int x = target.getLocation().getBlockX() + dx;
                        int z = target.getLocation().getBlockZ() + dz;
                        int y;
                        if (targetWorld.getEnvironment() == World.Environment.NETHER && alwaysUp) {
                            int refY = target.getLocation().getBlockY();
                            Integer fy = findSafeYAbove(targetWorld, x, z, refY, allowWater);
                            if (fy != null) y = fy;
                            else
                                y = ThreadLocalRandom.current().nextInt(1, Math.max(2, targetWorld.getMaxHeight() - 1));
                        } else if (alwaysUp) {
                            y = targetWorld.getHighestBlockYAt(x, z) + 1;
                        } else {
                            y = ThreadLocalRandom.current().nextInt(5, Math.max(6, targetWorld.getMaxHeight() - 1));
                        }
                        Location loc = new Location(targetWorld, x + 0.5, y, z + 0.5);
                        if (isSafe(loc, allowWater)) {
                            found = loc;
                            break;
                        }
                    }

                    if (found == null) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null)
                                p.sendMessage(TextUtil.parsePlaceholders(p, section.getString("messages.no_locations")));
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
                        if (cd > 0 && !p.hasPermission("enumrtp.player.bypass"))
                            cooldownManager.setCooldown(uuid, "player", cd);
                    });
                    return;
                }

                case "base": {
                    ConfigurationSection regions = cfg.getConfig().getConfigurationSection("regions");
                    if (regions == null || regions.getKeys(false).isEmpty()) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null)
                                p.sendMessage(TextUtil.parsePlaceholders(p, section.getString("messages.no_locations")));
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
                            if (p != null)
                                p.sendMessage(TextUtil.parsePlaceholders(p, section.getString("messages.no_locations")));
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
                        int y;
                        if (rw.getEnvironment() == World.Environment.NETHER && alwaysUp) {
                            int refY = center.getBlockY();
                            Integer fy = findSafeYAbove(rw, x, z, refY, allowWater);
                            if (fy != null) y = fy;
                            else y = ThreadLocalRandom.current().nextInt(1, Math.max(2, rw.getMaxHeight() - 1));
                        } else if (alwaysUp) {
                            y = rw.getHighestBlockYAt(x, z) + 1;
                        } else {
                            y = ThreadLocalRandom.current().nextInt(5, Math.max(6, rw.getMaxHeight() - 1));
                        }
                        Location loc = new Location(rw, x + 0.5, y, z + 0.5);
                        if (isSafe(loc, allowWater)) {
                            found = loc;
                            break;
                        }
                    }
                    if (found == null) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null)
                                p.sendMessage(TextUtil.parsePlaceholders(p, section.getString("messages.no_locations")));
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
                        if (cd > 0 && !p.hasPermission("enumrtp.base.bypass"))
                            cooldownManager.setCooldown(uuid, "base", cd);
                    });
                    return;
                }
                case "safe":
                case "far": {
                    int cx = 0;
                    int cz = 0;
                    Player refPlayer = Bukkit.getPlayer(uuid);
                    int refPlayerY = refPlayer != null ? refPlayer.getLocation().getBlockY() : 64;
                    for (int i = 0; i < tries; i++) {
                        int dx = ThreadLocalRandom.current().nextInt(min, max + 1) * (ThreadLocalRandom.current().nextBoolean() ? 1 : -1);
                        int dz = ThreadLocalRandom.current().nextInt(min, max + 1) * (ThreadLocalRandom.current().nextBoolean() ? 1 : -1);
                        int x = cx + dx;
                        int z = cz + dz;
                        int y;
                        if (world.getEnvironment() == World.Environment.NETHER && alwaysUp) {
                            Integer fy = findSafeYAbove(world, x, z, refPlayerY, allowWater);
                            if (fy != null) y = fy;
                            else y = ThreadLocalRandom.current().nextInt(1, Math.max(2, world.getMaxHeight() - 1));
                        } else if (alwaysUp) {
                            y = world.getHighestBlockYAt(x, z) + 1;
                        } else {
                            y = ThreadLocalRandom.current().nextInt(5, Math.max(6, world.getMaxHeight() - 1));
                        }
                        Location loc = new Location(world, x + 0.5, y, z + 0.5);
                        if (isSafe(loc, allowWater)) {
                            found = loc;
                            break;
                        }
                    }
                    if (found == null) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Player p2 = Bukkit.getPlayer(uuid);
                            if (p2 != null)
                                p2.sendMessage(TextUtil.parsePlaceholders(p2, section.getString("messages.no_locations")));
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
                        if (cd > 0 && !p2.hasPermission("enumrtp." + type + ".bypass"))
                            cooldownManager.setCooldown(uuid, type, cd);
                    });
                    return;
                }
                default: {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) p.sendMessage(TextUtil.colorize("&cНеподдерживаемый тип телепорта."));
                    });
                    return;
                }
            }
        } catch (Throwable t) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null)
                    p.sendMessage(TextUtil.colorize("&cОшибка при попытке найти локацию. Попробуйте ещё раз."));
            });
        }
    }

    private Integer findSafeYAbove(World world, int x, int z, int startY, boolean allowWater) {
        if (world == null) return null;
        int maxH = Math.max(1, world.getMaxHeight() - 1);
        if (world.getEnvironment() == World.Environment.NETHER) {
            int downStart = Math.min(127, maxH);
            for (int y = downStart; y >= 1; y--) {
                if (isSafeAt(world, x, y, z, allowWater)) return y;
            }
            for (int y = 129; y <= maxH; y++) {
                if (isSafeAt(world, x, y, z, allowWater)) return y;
            }
            return null;
        } else {
            int yStart = Math.max(1, startY);
            for (int y = yStart; y <= maxH; y++) {
                if (isSafeAt(world, x, y, z, allowWater)) return y;
            }
            return null;
        }
    }


    private boolean isSafeAt(World w, int bx, int by, int bz, boolean allowWater) {
        if (w == null) return false;
        try {
            Block feet = w.getBlockAt(bx, by, bz);
            Block head = w.getBlockAt(bx, by + 1, bz);
            Block below = w.getBlockAt(bx, by - 1, bz);
            if (feet.getType().isSolid() || head.getType().isSolid()) return false;
            if (!below.getType().isSolid()) return false;
            if (!allowWater) {
                Material ft = feet.getType();
                Material ht = head.getType();
                if (ft == Material.WATER || ht == Material.WATER) return false;
                if (ft == Material.LAVA || ht == Material.LAVA) return false;
            }
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private String formatCoords(Location loc) {
        if (loc == null) return "";
        return "x:" + loc.getBlockX() + " y:" + loc.getBlockY() + " z:" + loc.getBlockZ();
    }

    private boolean isSafe(Location loc, boolean allowWater) {
        if (loc == null) return false;
        try {
            World w = loc.getWorld();
            if (w == null) return false;
            Block feet = w.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            Block head = w.getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ());
            Block below = w.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
            if (feet.getType().isSolid() || head.getType().isSolid()) return false;
            if (!below.getType().isSolid()) return false;
            if (!allowWater) {
                Material ft = feet.getType();
                Material ht = head.getType();
                if (ft == Material.WATER || ht == Material.WATER) return false;
                if (ft == Material.LAVA || ht == Material.LAVA) return false;
            }
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public void shutdown() {
    }
}