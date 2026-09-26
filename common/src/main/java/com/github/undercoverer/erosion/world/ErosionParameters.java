package com.github.undercoverer.erosion.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Parameters for the erosion terrain world type. Defaults match the shader.
 */
public final class ErosionParameters {
    public static ErosionParameters defaults() {
        return new ErosionParameters(
                0.1928, 0.4, 0.41, 3.0,
                0.47, 0.41, 1.0, 4.0,
                1.1575, 3.0, 3.425, 1.095,
                0.33, 0.79,
                5, 3.0, 0.2495, 1.226, 0.39,
                -1.0, 0.0,
                0.715, 1.064, 3, 2.1025, 0.488,
                4096.0, -64, 320);
    }
    public static final Codec<ErosionParameters> CODEC = RecordCodecBuilder.create(i -> {
        ErosionParameters d = ErosionParameters.defaults();
        Core defaultCore = new Core(d.erosionScale, d.erosionStrength, d.gullyWeight, d.detail);
        Shape defaultShape = new Shape(d.roundingRidge, d.roundingCrease, d.roundingInit,
                d.roundingOctaveMult, d.onsetInit, d.onsetOctave, d.onsetRidgeInit, d.onsetRidgeOctave,
                d.assumedSlopeValue, d.assumedSlopeAmount);
        Octaves defaultOctaves = new Octaves(d.octaves, d.lacunarity, d.gain, d.cellScale, d.normalization);
        Terrain defaultTerrain = new Terrain(d.heightOffsetValue, d.heightOffsetFadeMix,
                d.heightFrequency, d.heightAmp, d.heightOctaves, d.heightLacunarity, d.heightGain);
        Mapping defaultMapping = new Mapping(d.blocksPerUnit, d.minY, d.maxY);
        return i.group(
                Core.CODEC.optionalFieldOf("erosion", defaultCore).forGetter(p -> new Core(p.erosionScale, p.erosionStrength, p.gullyWeight, p.detail)),
                Shape.CODEC.optionalFieldOf("shape", defaultShape).forGetter(p -> new Shape(p.roundingRidge, p.roundingCrease, p.roundingInit,
                        p.roundingOctaveMult, p.onsetInit, p.onsetOctave, p.onsetRidgeInit, p.onsetRidgeOctave,
                        p.assumedSlopeValue, p.assumedSlopeAmount)),
                Octaves.CODEC.optionalFieldOf("octaves", defaultOctaves).forGetter(p -> new Octaves(p.octaves, p.lacunarity, p.gain, p.cellScale, p.normalization)),
                Terrain.CODEC.optionalFieldOf("terrain", defaultTerrain).forGetter(p -> new Terrain(p.heightOffsetValue, p.heightOffsetFadeMix,
                        p.heightFrequency, p.heightAmp, p.heightOctaves, p.heightLacunarity, p.heightGain)),
                Mapping.CODEC.optionalFieldOf("mapping", defaultMapping).forGetter(p -> new Mapping(p.blocksPerUnit, p.minY, p.maxY))
        ).apply(i, (core, shape, octaves, terrain, mapping) -> new ErosionParameters(
                core.erosionScale, core.erosionStrength, core.gullyWeight, core.detail,
                shape.roundingRidge, shape.roundingCrease, shape.roundingInit, shape.roundingOctaveMult,
                shape.onsetInit, shape.onsetOctave, shape.onsetRidgeInit, shape.onsetRidgeOctave,
                shape.assumedSlopeValue, shape.assumedSlopeAmount,
                octaves.octaves, octaves.lacunarity, octaves.gain, octaves.cellScale, octaves.normalization,
                terrain.heightOffsetValue, terrain.heightOffsetFadeMix,
                terrain.heightFrequency, terrain.heightAmp, terrain.heightOctaves,
                terrain.heightLacunarity, terrain.heightGain,
                mapping.blocksPerUnit, mapping.minY, mapping.maxY));
    });
    /**
     * The scale of the erosion effect, affecting it both horizontally and vertically.
     */
    public final double erosionScale;
    /**
     * The strength of the erosion effect, affecting the magnitude of all octaves, and indirectly affecting the directions of the gullies as a result.
     */
    public final double erosionStrength;
    /**
     * The magnitude of the gullies as a weight value from 0 to 1.
     * A value of 0 can sharpen peaks and valleys but feature virtually no gullies.
     * A value of 1 produces full gullies but may leave peaks and valleys rounded.
     * Adjusting erosion gully weight while inversely adjusting erosion scale can be
     * used to control the sharpness of peaks and valleys while leaving gully
     * magnitudes largely untocuhed.
     */
    public final double gullyWeight;

    /**
     * The overall detail of the erosion. Lower values restrict the effect of higher
     * frequency gullies to steeper slopes.
     */
    public final double detail;
    // Separate rounding control of ridges and creases.
    public final double roundingRidge;
    public final double roundingCrease;

    /**
     * Multiplier applied to the initial height function.
     * E.g. if the height function has noise of 5 times lower frequency
     * than the largest gullies, a value of 0.2 can compensate for that.
     */
    public final double roundingInit;

    // Control over how far away from ridges/creases the erosion takes effect.
    /**
     * Multiplier applied to each subsequent gully octave after the first.
     * Setting it to the same value as the erosion lacunarity will produce
     * consistent rounding of all octaves.
     */
    public final double roundingOctaveMult;
    /**
     * Onset used on the initial height function.
     */
    public final double onsetInit;
    /**
     * Onset used on each gully octave.
     */
    public final double onsetOctave;

    /**
     * RidgeMap-specific onset used on the initial height function.
     */
    public final double onsetRidgeInit;

    // Control over the assumed slope of the initial height function.
    // In practise, assuming a slope can work better than using the input slope,
    // since the final terrain can be shaped quite differently than the input.
    /**
     * RidgeMap-specific onset used on each gully octave.
     */
    public final double onsetRidgeOctave;
    /**
     * An assumed slope value to override the actual slope.
     */
    public final double assumedSlopeValue;
    /**
     * The amount (from 0 to 1) to override the actual slope.
     */
    public final double assumedSlopeAmount;
    /**
     * Control over the erosion octaves, with each successive octave layering
     * smaller gullies onto the terrain.
     */
    public final int octaves;
    /**
     * The lacunarity controls the frequency (the inverse
     * horizontal scale) of each octave relative to the last.
     */
    public final double lacunarity;
    /**
     * The gain controls the magnitude (the vertical
     * scale) of each octave relative to the last.
     */
    public final double gain;

    /**
     * Gullies are based on stripes within Voronoi-like cells in the Phacelle noise
     * function. The cell scale parameter controls the sizes of the cells relative
     * to the overall erosion scale, while keeping the stripe widths unaffected.
     * Values close to 1 usually produce good results. Smaller values produce more
     * grainy gullies while larger values produce longer unbroken gullies, but too
     * large values produce chaotic curved gullies that are not aligned with the
     * slopes. Value changes can cause abrupt changes in output, especially far away
     * from the origin, so this parameter is not well suited for animation or for
     * modulation by other functions.
     */
    public final double cellScale;
    // Terrain (unrelated to erosion)
    /**
     * The degree of normalization applied in the Phacelle noise, between 0 and 1.
     * The erosion filter depends on a certain consistency in magnitude of the
     * Phacelle output. However, high values can create loopy results where ridges
     * and creases meet up at a point, which produces unnatural looking results.
     */
    public final double normalization;
    /**
     * An offset value between -1 and 1, where a value of -1 only lowers, while
     * 1 only raises. The offset is proportional to the erosion strength
     * parameter, so if that parameter is the same for the entire terrain, the
     * effect of the height offset will move the entire terrain surface up or
     * down by the same emount.
     */
    public final double heightOffsetValue;
    /**
     * A value between 0 and 1 which is the degree to which the offset value is
     * replaced by the negated erosion fade target value. This has the effect
     * of only raising at valleys and only lowering at peaks, which, due to how
     * the erosion filter works, has the effect of largely preserving the minima
     * and maxima of the terrain.
     */
    public final double heightOffsetFadeMix;
    /**
     * The inverse horizontal scale of the terrain noise function.
     */
    public final double heightFrequency;
    /**
     * The vertical scale (amplitude) of the terrain noise function.
     */
    public final double heightAmp;
    /**
     * Control over the noise function octaves, with each successive
     * octave layering smaller bumps onto the terrain.
     */
    public final int heightOctaves;

    /**
     * The lacunarity controls the frequency (the inverse
     * horizontal scale) of each octave relative to the last.
     */
    public final double heightLacunarity;
    // Minecraft stuff
    /**
     * The gain controls the magnitude (the vertical scale)
     * of each octave relative to the last.
     */
    public final double heightGain;
    /**
     * Extra horizontal scaling
     */
    public final double blocksPerUnit;
    public final int minY;

    public final int maxY;

    public ErosionParameters(
            double erosionScale, double erosionStrength, double gullyWeight, double detail,
            double roundingRidge, double roundingCrease, double roundingInit, double roundingOctaveMult,
            double onsetInit, double onsetOctave, double onsetRidgeInit, double onsetRidgeOctave,
            double assumedSlopeValue, double assumedSlopeAmount,
            int octaves, double lacunarity, double gain, double cellScale, double normalization,
            double heightOffsetValue, double heightOffsetFadeMix,
            double heightFrequency, double heightAmp, int heightOctaves,
            double heightLacunarity, double heightGain,
            double blocksPerUnit, int minY, int maxY) {
        this.erosionScale = erosionScale;
        this.erosionStrength = erosionStrength;
        this.gullyWeight = gullyWeight;
        this.detail = detail;
        this.roundingRidge = roundingRidge;
        this.roundingCrease = roundingCrease;
        this.roundingInit = roundingInit;
        this.roundingOctaveMult = roundingOctaveMult;
        this.onsetInit = onsetInit;
        this.onsetOctave = onsetOctave;
        this.onsetRidgeInit = onsetRidgeInit;
        this.onsetRidgeOctave = onsetRidgeOctave;
        this.assumedSlopeValue = assumedSlopeValue;
        this.assumedSlopeAmount = assumedSlopeAmount;
        this.octaves = octaves;
        this.lacunarity = lacunarity;
        this.gain = gain;
        this.cellScale = cellScale;
        this.normalization = normalization;
        this.heightOffsetValue = heightOffsetValue;
        this.heightOffsetFadeMix = heightOffsetFadeMix;
        this.heightFrequency = heightFrequency;
        this.heightAmp = heightAmp;
        this.heightOctaves = heightOctaves;
        this.heightLacunarity = heightLacunarity;
        this.heightGain = heightGain;
        this.blocksPerUnit = blocksPerUnit;
        this.minY = minY;
        this.maxY = maxY;
    }

    public static Builder toBuilder(ErosionParameters p) {
        return new Builder(p);
    }

    public static final class Builder {
        private double erosionScale, erosionStrength, gullyWeight, detail;
        private double roundingRidge, roundingCrease, roundingInit, roundingOctaveMult;
        private double onsetInit, onsetOctave, onsetRidgeInit, onsetRidgeOctave;
        private double assumedSlopeValue, assumedSlopeAmount;
        private int octaves;
        private double lacunarity, gain, cellScale, normalization;
        private double heightOffsetValue, heightOffsetFadeMix;
        private double heightFrequency, heightAmp;
        private int heightOctaves;
        private double heightLacunarity, heightGain;
        private double blocksPerUnit;
        private int minY, maxY;

        private Builder(ErosionParameters p) {
            this.erosionScale = p.erosionScale;
            this.erosionStrength = p.erosionStrength;
            this.gullyWeight = p.gullyWeight;
            this.detail = p.detail;
            this.roundingRidge = p.roundingRidge;
            this.roundingCrease = p.roundingCrease;
            this.roundingInit = p.roundingInit;
            this.roundingOctaveMult = p.roundingOctaveMult;
            this.onsetInit = p.onsetInit;
            this.onsetOctave = p.onsetOctave;
            this.onsetRidgeInit = p.onsetRidgeInit;
            this.onsetRidgeOctave = p.onsetRidgeOctave;
            this.assumedSlopeValue = p.assumedSlopeValue;
            this.assumedSlopeAmount = p.assumedSlopeAmount;
            this.octaves = p.octaves;
            this.lacunarity = p.lacunarity;
            this.gain = p.gain;
            this.cellScale = p.cellScale;
            this.normalization = p.normalization;
            this.heightOffsetValue = p.heightOffsetValue;
            this.heightOffsetFadeMix = p.heightOffsetFadeMix;
            this.heightFrequency = p.heightFrequency;
            this.heightAmp = p.heightAmp;
            this.heightOctaves = p.heightOctaves;
            this.heightLacunarity = p.heightLacunarity;
            this.heightGain = p.heightGain;
            this.blocksPerUnit = p.blocksPerUnit;
            this.minY = p.minY;
            this.maxY = p.maxY;
        }

        public Builder erosionScale(double v) {
            erosionScale = v;
            return this;
        }

        public Builder erosionStrength(double v) {
            erosionStrength = v;
            return this;
        }

        public Builder gullyWeight(double v) {
            gullyWeight = v;
            return this;
        }

        public Builder detail(double v) {
            detail = v;
            return this;
        }

        public Builder roundingRidge(double v) {
            roundingRidge = v;
            return this;
        }

        public Builder roundingCrease(double v) {
            roundingCrease = v;
            return this;
        }

        public Builder roundingInit(double v) {
            roundingInit = v;
            return this;
        }

        public Builder roundingOctaveMult(double v) {
            roundingOctaveMult = v;
            return this;
        }

        public Builder onsetInit(double v) {
            onsetInit = v;
            return this;
        }

        public Builder onsetOctave(double v) {
            onsetOctave = v;
            return this;
        }

        public Builder onsetRidgeInit(double v) {
            onsetRidgeInit = v;
            return this;
        }

        public Builder onsetRidgeOctave(double v) {
            onsetRidgeOctave = v;
            return this;
        }

        public Builder assumedSlopeValue(double v) {
            assumedSlopeValue = v;
            return this;
        }

        public Builder assumedSlopeAmount(double v) {
            assumedSlopeAmount = v;
            return this;
        }

        public Builder octaves(int v) {
            octaves = v;
            return this;
        }

        public Builder lacunarity(double v) {
            lacunarity = v;
            return this;
        }

        public Builder gain(double v) {
            gain = v;
            return this;
        }

        public Builder cellScale(double v) {
            cellScale = v;
            return this;
        }

        public Builder normalization(double v) {
            normalization = v;
            return this;
        }

        public Builder heightOffsetValue(double v) {
            heightOffsetValue = v;
            return this;
        }

        public Builder heightOffsetFadeMix(double v) {
            heightOffsetFadeMix = v;
            return this;
        }

        public Builder heightFrequency(double v) {
            heightFrequency = v;
            return this;
        }

        public Builder heightAmp(double v) {
            heightAmp = v;
            return this;
        }

        public Builder heightOctaves(int v) {
            heightOctaves = v;
            return this;
        }

        public Builder heightLacunarity(double v) {
            heightLacunarity = v;
            return this;
        }

        public Builder heightGain(double v) {
            heightGain = v;
            return this;
        }

        public Builder blocksPerUnit(double v) {
            blocksPerUnit = v;
            return this;
        }

        public ErosionParameters build() {
            return new ErosionParameters(
                    erosionScale, erosionStrength, gullyWeight, detail,
                    roundingRidge, roundingCrease, roundingInit, roundingOctaveMult,
                    onsetInit, onsetOctave, onsetRidgeInit, onsetRidgeOctave,
                    assumedSlopeValue, assumedSlopeAmount,
                    octaves, lacunarity, gain, cellScale, normalization,
                    heightOffsetValue, heightOffsetFadeMix,
                    heightFrequency, heightAmp, heightOctaves, heightLacunarity, heightGain,
                    blocksPerUnit, minY, maxY);
        }
    }

    // RecordCodecBuilder can only do 16 fields per group
    private record Core(double erosionScale, double erosionStrength, double gullyWeight, double detail) {
        static final Codec<Core> CODEC = RecordCodecBuilder.create(i -> {
            ErosionParameters d = ErosionParameters.defaults();
            return i.group(
                    Codec.DOUBLE.optionalFieldOf("erosion_scale", d.erosionScale).forGetter(Core::erosionScale),
                    Codec.DOUBLE.optionalFieldOf("erosion_strength", d.erosionStrength).forGetter(Core::erosionStrength),
                    Codec.DOUBLE.optionalFieldOf("gully_weight", d.gullyWeight).forGetter(Core::gullyWeight),
                    Codec.DOUBLE.optionalFieldOf("detail", d.detail).forGetter(Core::detail)
            ).apply(i, Core::new);
        });
    }

    private record Shape(double roundingRidge, double roundingCrease, double roundingInit,
                         double roundingOctaveMult, double onsetInit, double onsetOctave,
                         double onsetRidgeInit, double onsetRidgeOctave,
                         double assumedSlopeValue, double assumedSlopeAmount) {
        static final Codec<Shape> CODEC = RecordCodecBuilder.create(i -> {
            ErosionParameters d = ErosionParameters.defaults();
            return i.group(
                    Codec.DOUBLE.optionalFieldOf("rounding_ridge", d.roundingRidge).forGetter(Shape::roundingRidge),
                    Codec.DOUBLE.optionalFieldOf("rounding_crease", d.roundingCrease).forGetter(Shape::roundingCrease),
                    Codec.DOUBLE.optionalFieldOf("rounding_init", d.roundingInit).forGetter(Shape::roundingInit),
                    Codec.DOUBLE.optionalFieldOf("rounding_octave_mult", d.roundingOctaveMult).forGetter(Shape::roundingOctaveMult),
                    Codec.DOUBLE.optionalFieldOf("onset_init", d.onsetInit).forGetter(Shape::onsetInit),
                    Codec.DOUBLE.optionalFieldOf("onset_octave", d.onsetOctave).forGetter(Shape::onsetOctave),
                    Codec.DOUBLE.optionalFieldOf("onset_ridge_init", d.onsetRidgeInit).forGetter(Shape::onsetRidgeInit),
                    Codec.DOUBLE.optionalFieldOf("onset_ridge_octave", d.onsetRidgeOctave).forGetter(Shape::onsetRidgeOctave),
                    Codec.DOUBLE.optionalFieldOf("assumed_slope_value", d.assumedSlopeValue).forGetter(Shape::assumedSlopeValue),
                    Codec.DOUBLE.optionalFieldOf("assumed_slope_amount", d.assumedSlopeAmount).forGetter(Shape::assumedSlopeAmount)
            ).apply(i, Shape::new);
        });
    }

    private record Octaves(int octaves, double lacunarity, double gain,
                           double cellScale, double normalization) {
        static final Codec<Octaves> CODEC = RecordCodecBuilder.create(i -> {
            ErosionParameters d = ErosionParameters.defaults();
            return i.group(
                    Codec.INT.optionalFieldOf("octaves", d.octaves).forGetter(Octaves::octaves),
                    Codec.DOUBLE.optionalFieldOf("lacunarity", d.lacunarity).forGetter(Octaves::lacunarity),
                    Codec.DOUBLE.optionalFieldOf("gain", d.gain).forGetter(Octaves::gain),
                    Codec.DOUBLE.optionalFieldOf("cell_scale", d.cellScale).forGetter(Octaves::cellScale),
                    Codec.DOUBLE.optionalFieldOf("normalization", d.normalization).forGetter(Octaves::normalization)
            ).apply(i, Octaves::new);
        });
    }

    private record Terrain(double heightOffsetValue, double heightOffsetFadeMix,
                           double heightFrequency, double heightAmp, int heightOctaves,
                           double heightLacunarity, double heightGain) {
        static final Codec<Terrain> CODEC = RecordCodecBuilder.create(i -> {
            ErosionParameters d = ErosionParameters.defaults();
            return i.group(
                    Codec.DOUBLE.optionalFieldOf("height_offset_value", d.heightOffsetValue).forGetter(Terrain::heightOffsetValue),
                    Codec.DOUBLE.optionalFieldOf("height_offset_fade_mix", d.heightOffsetFadeMix).forGetter(Terrain::heightOffsetFadeMix),
                    Codec.DOUBLE.optionalFieldOf("height_frequency", d.heightFrequency).forGetter(Terrain::heightFrequency),
                    Codec.DOUBLE.optionalFieldOf("height_amp", d.heightAmp).forGetter(Terrain::heightAmp),
                    Codec.INT.optionalFieldOf("height_octaves", d.heightOctaves).forGetter(Terrain::heightOctaves),
                    Codec.DOUBLE.optionalFieldOf("height_lacunarity", d.heightLacunarity).forGetter(Terrain::heightLacunarity),
                    Codec.DOUBLE.optionalFieldOf("height_gain", d.heightGain).forGetter(Terrain::heightGain)
            ).apply(i, Terrain::new);
        });
    }

    private record Mapping(double blocksPerUnit, int minY, int maxY) {
        static final Codec<Mapping> CODEC = RecordCodecBuilder.create(i -> {
            ErosionParameters d = ErosionParameters.defaults();
            return i.group(
                    Codec.DOUBLE.optionalFieldOf("blocks_per_unit", d.blocksPerUnit).forGetter(Mapping::blocksPerUnit),
                    Codec.INT.optionalFieldOf("min_y", d.minY).forGetter(Mapping::minY),
                    Codec.INT.optionalFieldOf("max_y", d.maxY).forGetter(Mapping::maxY)
            ).apply(i, Mapping::new);
        });
    }
}
