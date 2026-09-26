package com.github.undercoverer.erosion.world;


import net.minecraft.util.Mth;

import static net.minecraft.util.Mth.lerp;

public final class ErosionHeightmap {

    private final long seed;
    private final ErosionParameters ep;
    private final ThreadLocal<ErosionFilter.Result> erosionOut = ThreadLocal.withInitial(ErosionFilter.Result::new);
    private final ThreadLocal<double[]> noisedOut = ThreadLocal.withInitial(() -> new double[3]);
    private final ThreadLocal<Sample> sampleOut = ThreadLocal.withInitial(Sample::new);

    public ErosionHeightmap(long seed, ErosionParameters erosionParameters) {
        this.seed = seed;
        this.ep = erosionParameters;
    }

    public void sample(int blockX, int blockZ, Sample out) {
        double unitX = blockX / ep.blocksPerUnit;
        double unitY = blockZ / ep.blocksPerUnit;

        // Calculate the FBM terrain height and derivatives and store them in n.
        // The heights are in the [-1, 1] range.
        double[] n = ErosionNoise.fractalNoise(seed, unitX, unitY, ep.heightFrequency, ep.heightOctaves, ep.heightLacunarity, ep.heightGain, noisedOut.get());
        double nH = n[0] * ep.heightAmp;
        double nSx = n[1] * ep.heightAmp; // heightFunctionScale = 1 in the shader demo
        double nSz = n[2] * ep.heightAmp;

        // Define the erosion fade target based on the altitude of the pre-eroded terrain.
        // The fade target should strive to be -1 at valleys and 1 at peaks, but overshooting is ok.
        double fadeTarget = Math.max(-1.0, Math.min(1.0, nH / (ep.heightAmp * 0.6)));

        // Change terrain heights from [-1, 1] range to [0, 1] range.
        nH = nH * 0.5 + 0.5;

        // The output ridge map is -1 on creases and 1 on ridges
        ErosionFilter.Result erosion = erosionOut.get();
        ErosionFilter.apply(seed, unitX, unitY, nH, nSx, nSz, fadeTarget, ep, erosion);

        // Offset according to the height offset parameter by multiplying it with the magnitude.
        double offset = lerp(ep.heightOffsetFadeMix, ep.heightOffsetValue, -fadeTarget) * erosion.magnitude;
        out.height = nH + erosion.heightDelta + offset;
        out.ridgeMap = erosion.ridgeMap;
        out.erosion = erosion.magnitude > 1e-10 ? erosion.heightDelta / erosion.magnitude : 0.0;
    }


    private static final double SHALLOW_WATER_LEVEL = (55.0 - (-64.0)) / (320.0 - (-64.0));
    private static final double TARGET_FLOOR = (8.0 - (-64.0)) / (320.0 - (-64.0));
    private static final double MAX_RAW_DEPTH = SHALLOW_WATER_LEVEL - 0.1; //TODO Figure out what 0.1 should actually be

    private static double compressDepth(double h) {
        if (h >= SHALLOW_WATER_LEVEL) return h;
        double depth = SHALLOW_WATER_LEVEL - h;
        double t = Mth.clamp(depth / MAX_RAW_DEPTH, 0.0, 1.0);
        double compressed = Math.sqrt(t);
        double newDepth = compressed * (SHALLOW_WATER_LEVEL - TARGET_FLOOR);
        return Mth.clamp(SHALLOW_WATER_LEVEL - newDepth, TARGET_FLOOR, SHALLOW_WATER_LEVEL);
    }

    // TODO Move this into erosion parameters
    private static final double HEIGHT_EXAGGERATION = 1.2;

    public double height(int blockX, int blockZ) {
        Sample s = sampleOut.get();
        sample(blockX, blockZ, s);

        double h = compressDepth(s.height);

        // Height is normalized over minY - maxY
        return SHALLOW_WATER_LEVEL + (h - SHALLOW_WATER_LEVEL) * HEIGHT_EXAGGERATION;
    }

    public static final class Sample {
        public double height;
        public double ridgeMap;
        public double erosion;
    }

}