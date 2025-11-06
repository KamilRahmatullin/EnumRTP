package com.enumdev.enumrtp.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {
    private final Map<UUID, Map<String, Long>> data = new ConcurrentHashMap<>();

    public boolean isOnCooldown(UUID u, String key) {
        Map<String, Long> m = data.get(u);
        if (m == null) return false;
        Long t = m.get(key);
        if (t == null) return false;
        if (t <= System.currentTimeMillis()) {
            m.remove(key);
            return false;
        }
        return true;
    }

    public long getRemaining(UUID u, String key) {
        Map<String, Long> m = data.get(u);
        if (m == null) return 0;
        Long t = m.get(key);
        if (t == null) return 0;
        long rem = (t - System.currentTimeMillis()) / 1000L;
        return Math.max(0, rem);
    }

    public void setCooldown(UUID u, String key, long seconds) {
        data.computeIfAbsent(u, k -> new ConcurrentHashMap<>()).put(key, System.currentTimeMillis() + seconds * 1000L);
    }

    public void removePlayer(UUID u) { data.remove(u); }

    public void shutdown() { data.clear(); }
}
