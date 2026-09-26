package com.github.undercoverer.erosion;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Erosion implements ModInitializer {
    public static final String MOD_ID = "erosion";
    private static final Logger LOG = LoggerFactory.getLogger(Erosion.class);

    @Override
    public void onInitialize() {
        LOG.info("Initializing erosion");
        Lifecycle.registerDensityFunctionCodecs((id, codec) -> Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, id, codec));

        ServerWorldEvents.LOAD.register((server, world) -> Lifecycle.onWorldLoad(world));
    }
}
