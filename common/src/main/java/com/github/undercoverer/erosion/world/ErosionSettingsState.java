package com.github.undercoverer.erosion.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.saveddata.SavedData;


public final class ErosionSettingsState extends SavedData {

    public static final String STORAGE_KEY = "erosion_settings";

    private static final Codec<ErosionSettingsState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ErosionParameters.CODEC.optionalFieldOf("parameters", ErosionParameters.defaults())
                    .forGetter(ErosionSettingsState::getParameters),
            Codec.BOOL.optionalFieldOf("explicit_parameters", false)
                    .forGetter(ErosionSettingsState::isExplicit)
    ).apply(instance, ErosionSettingsState::new));
    public static final SavedData.Factory<ErosionSettingsState> TYPE =
            new SavedData.Factory<>(
                    ErosionSettingsState::createDefault,
                    ErosionSettingsState::fromNbt,
                    null
            );
    private ErosionParameters parameters;
    private boolean explicit;

    private ErosionSettingsState(ErosionParameters parameters, boolean explicit) {
        this.parameters = parameters;
        this.explicit = explicit;
    }

    public static ErosionSettingsState createDefault() {
        return new ErosionSettingsState(ErosionParameters.defaults(), false);
    }

    public static ErosionSettingsState fromNbt(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        return CODEC.parse(NbtOps.INSTANCE, nbt)
                .result()
                .orElseGet(ErosionSettingsState::createDefault);
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        CODEC.encodeStart(NbtOps.INSTANCE, this)
                .result()
                .ifPresent(encoded -> nbt.merge((CompoundTag) encoded));
        return nbt;
    }

    public ErosionParameters getParameters() {
        return parameters;
    }

    public void setParameters(ErosionParameters newParameters) {
        this.parameters = newParameters;
        this.explicit = true;
        setDirty();
    }

    public boolean isExplicit() {
        return explicit;
    }
}
