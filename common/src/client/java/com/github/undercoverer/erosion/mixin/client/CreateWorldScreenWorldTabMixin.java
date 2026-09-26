package com.github.undercoverer.erosion.mixin.client;

import com.github.undercoverer.erosion.client.ErosionSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$WorldTab")
public abstract class CreateWorldScreenWorldTabMixin {
    private static final ResourceKey<WorldPreset> EROSION_PRESET_KEY =
            ResourceKey.create(Registries.WORLD_PRESET, ResourceLocation.fromNamespaceAndPath("erosion", "erosion"));
    @Shadow
    @Final
    private Button customizeTypeButton;
    @Unique
    private CreateWorldScreen erosion$createWorldScreen;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void erosion$captureCreateWorldScreen(CreateWorldScreen createWorldScreen, CallbackInfo callbackInfo) {
        this.erosion$createWorldScreen = createWorldScreen;
        createWorldScreen.getUiState().addListener(state -> erosion$refreshCustomizeButton());
        erosion$refreshCustomizeButton();
    }

    @Unique
    private void erosion$refreshCustomizeButton() {
        if (erosion$isErosionWorldTypeSelected()) {
            this.customizeTypeButton.active = true;
        }
    }

    @Inject(method = "openPresetEditor", at = @At("HEAD"), cancellable = true)
    private void erosion$openTerrainScaleScreen(CallbackInfo callbackInfo) {
        Minecraft minecraftClient = Minecraft.getInstance();
        if (minecraftClient == null || erosion$createWorldScreen == null) {
            return;
        }
        if (erosion$isErosionWorldTypeSelected()) {
            minecraftClient.setScreen(new ErosionSettingsScreen(erosion$createWorldScreen));
            callbackInfo.cancel();
        }
    }

    @Unique
    private boolean erosion$isErosionWorldTypeSelected() {
        if (erosion$createWorldScreen == null) {
            return false;
        }
        WorldCreationUiState worldCreator = erosion$createWorldScreen.getUiState();
        if (worldCreator == null) {
            return false;
        }
        WorldCreationUiState.WorldTypeEntry worldType = worldCreator.getWorldType();
        if (worldType == null) {
            return false;
        }
        return worldType.preset().unwrapKey().map(EROSION_PRESET_KEY::equals).orElse(false);
    }


}
