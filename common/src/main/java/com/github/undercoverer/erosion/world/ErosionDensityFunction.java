package com.github.undercoverer.erosion.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;


public final class ErosionDensityFunction implements DensityFunction {

    public static final MapCodec<ErosionDensityFunction> CODEC = MapCodec.unit(ErosionDensityFunction::new);

    @Override
    public double compute(DensityFunction.FunctionContext pos) {
        return ErosionSettingsManager.heightAt(pos.blockX(), pos.blockZ()) - pos.blockY();
    }

    @Override
    public void fillArray(double[] densities, DensityFunction.ContextProvider applier) {
        for (int i = 0; i < densities.length; i++) {
            DensityFunction.FunctionContext pos = applier.forIndex(i);
            densities[i] = ErosionSettingsManager.heightAt(pos.blockX(), pos.blockZ()) - pos.blockY();
        }
    }

    @Override
    public DensityFunction mapAll(DensityFunction.Visitor visitor) {
        return visitor.apply(this);
    }

    @Override
    public double minValue() {
        return -64;
    }

    @Override
    public double maxValue() {
        return 320;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KeyDispatchDataCodec.of(CODEC);
    }
}
