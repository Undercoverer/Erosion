package com.github.undercoverer.erosion;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Erosion.FML_MOD_ID)
public class Erosion {
    public static final String FML_MOD_ID = "erosion";
    private static final Logger LOG = LoggerFactory.getLogger(Erosion.class);

    public Erosion(IEventBus modEventBus) {
        LOG.info("Initializing erosion for NeoForge");
        modEventBus.addListener(this::onRegister);
        NeoForge.EVENT_BUS.addListener(this::onLevelLoad);
    }


    private void onRegister(RegisterEvent event) {
        event.register(Registries.DENSITY_FUNCTION_TYPE, helper ->
                Lifecycle.registerDensityFunctionCodecs(helper::register));
    }

    private void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel world) {
            Lifecycle.onWorldLoad(world);
        }
    }
}
