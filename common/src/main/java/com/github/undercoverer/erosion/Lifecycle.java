package com.github.undercoverer.erosion;


import com.github.undercoverer.erosion.world.ErosionDensityFunction;
import com.github.undercoverer.erosion.world.ErosionSettingsManager;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.DensityFunction;


public final class Lifecycle {
    public static final String MOD_ID = "erosion";

    public static void registerDensityFunctionCodecs(CodecRegistrar<MapCodec<? extends DensityFunction>> registrar) {
        registrar.register(ResourceLocation.fromNamespaceAndPath(MOD_ID, "erosion"), ErosionDensityFunction.CODEC);
    }

    public static void onWorldLoad(ServerLevel world) {
        if (world.dimension() == Level.OVERWORLD) {
            ErosionSettingsManager.initializeForWorld(world);
        }
    }

    @FunctionalInterface
    public interface CodecRegistrar<T> {
        void register(ResourceLocation id, T value);
    }
}
