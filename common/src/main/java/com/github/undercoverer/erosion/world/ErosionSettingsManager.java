package com.github.undercoverer.erosion.world;

import net.minecraft.server.level.ServerLevel;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ErosionSettingsManager {

    private static final int MAX_CACHE_ENTRIES = 65536;
    private static final Map<Long, Integer> HEIGHT_CACHE = new LinkedHashMap<>(65536, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Integer> eldest) {
            return size() > MAX_CACHE_ENTRIES;
        }
    };
    private static volatile ErosionHeightmap heightmap;
    private static volatile ErosionParameters p = ErosionParameters.defaults();

    public static synchronized void initializeForWorld(ServerLevel serverWorld) {
        ErosionSettingsState state = serverWorld.getDataStorage().computeIfAbsent(ErosionSettingsState.TYPE, ErosionSettingsState.STORAGE_KEY);

        if (!state.isExplicit()) {
            ErosionParameters pending = ErosionSelections.consume();
            state.setParameters(pending != null ? pending : ErosionParameters.defaults());
        }

        p = state.getParameters();
        heightmap = new ErosionHeightmap(serverWorld.getSeed(), p);
        // hehe
        synchronized (HEIGHT_CACHE) {
            HEIGHT_CACHE.clear();
        }
    }


    public static int heightAt(int blockX, int blockZ) {
        long key = pack(blockX, blockZ);
        synchronized (HEIGHT_CACHE) {
            Integer cached = HEIGHT_CACHE.get(key);
            if (cached != null) return cached;
        }

        int y = getY(blockX, blockZ);

        synchronized (HEIGHT_CACHE) {
            HEIGHT_CACHE.put(key, y);
        }
        return y;
    }

    private static int getY(int blockX, int blockZ) {
        return p.minY + (int) Math.round(heightmap.height(blockX, blockZ) * (p.maxY - p.minY));
    }

    private static long pack(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}
