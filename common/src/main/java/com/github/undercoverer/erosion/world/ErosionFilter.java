package com.github.undercoverer.erosion.world;

import net.minecraft.util.Mth;

import static net.minecraft.util.Mth.lerp;

/*
 * Advanced Terrain Erosion Filter copyright (c) 2025 Rune Skovbo Johansen
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Java port of the GLSL from the "Advanced Terrain Erosion Filter" by runevision on Shadertoy
 */
final class ErosionFilter {

    private static final ThreadLocal<double[]> randScratch = ThreadLocal.withInitial(() -> new double[2]);
    private static final ThreadLocal<double[]> phacelleScratch = ThreadLocal.withInitial(() -> new double[4]);

    private static double powInv(double t, double power) {
        return 1.0 - Math.pow(1.0 - Mth.clamp(t, 0.0, 1.0), power);
    }

    private static double easeOut(double t) {
        double v = 1.0 - Mth.clamp(t, 0.0, 1.0);
        return 1.0 - v * v;
    }

    private static double smoothStart(double t, double smoothing) {
        if (t >= smoothing) return t - 0.5 * smoothing;
        return 0.5 * t * t / smoothing;
    }

    /**
     * Apply the erosion filter at one point.
     *
     * @param seed              world seed
     * @param px,py             input point in shader units
     * @param inH,inSx,inSz     height, d/dx, d/dy of the input terrain at p
     * @param fadeTarget        -1 at valleys, 1 at peaks
     * @param erosionParameters parameter set
     * @param out               result object (to avoid allocation)
     */
    static void apply(long seed, double px, double py, double inH, double inSx, double inSz, double fadeTarget, ErosionParameters erosionParameters, Result out) {
        double strength = erosionParameters.erosionStrength * erosionParameters.erosionScale;
        fadeTarget = Mth.clamp(fadeTarget, -1.0, 1.0);

        double h = inH, sx = inSx, sz = inSz;

        double freq = 1.0 / (erosionParameters.erosionScale * erosionParameters.cellScale);
        double slopeLength = Math.max(Math.hypot(sx, sz), 1e-10);
        double magnitude = 0.0;
        double roundingMult = 1.0;

        double roundingForInput = lerp(Mth.clamp(fadeTarget + 0.5, 0.0, 1.0), erosionParameters.roundingRidge, erosionParameters.roundingCrease) * erosionParameters.roundingInit;
        double combiMask = easeOut(smoothStart(slopeLength * erosionParameters.onsetInit, roundingForInput * erosionParameters.onsetInit));

        double ridgeMapCombiMask = easeOut(slopeLength * erosionParameters.onsetRidgeInit);
        double ridgeMapFadeTarget = fadeTarget;

        double gullySx = lerp(erosionParameters.assumedSlopeAmount, sx, sx / slopeLength * erosionParameters.assumedSlopeValue);
        double gullySz = lerp(erosionParameters.assumedSlopeAmount, sz, sz / slopeLength * erosionParameters.assumedSlopeValue);

        double[] randOut = randScratch.get();
        double[] phacelle = phacelleScratch.get();

        for (int i = 0; i < erosionParameters.octaves; i++) {
            double gl = Math.hypot(gullySx, gullySz);
            double ndx = Math.abs(gl) > 1e-10 ? gullySx / gl : gullySx;
            double ndz = Math.abs(gl) > 1e-10 ? gullySz / gl : gullySz;
            PhacelleNoise.sample(seed, px * freq, py * freq, ndx, ndz, erosionParameters.cellScale, 0.25, erosionParameters.normalization, randOut, phacelle);
            double phX = phacelle[0], phY = phacelle[1];
            double phSx = phacelle[2] * -freq, phSz = phacelle[3] * -freq;
            double sloping = Math.abs(phY);

            double sign = Math.signum(phY);
            gullySx += sign * phSx * strength * erosionParameters.gullyWeight;
            gullySz += sign * phSz * strength * erosionParameters.gullyWeight;

            double gulH = phX, gulSx = phY * phSx, gulSz = phY * phSz;
            double fadedH = lerp(combiMask, fadeTarget, gulH * erosionParameters.gullyWeight);
            double fadedSx = lerp(combiMask, 0.0, gulSx * erosionParameters.gullyWeight);
            double fadedSz = lerp(combiMask, 0.0, gulSz * erosionParameters.gullyWeight);
            h += fadedH * strength;
            sx += fadedSx * strength;
            sz += fadedSz * strength;
            magnitude += strength;

            fadeTarget = fadedH;

            double roundingForOctave = lerp(Mth.clamp(phX + 0.5, 0.0, 1.0), erosionParameters.roundingRidge, erosionParameters.roundingCrease) * roundingMult;
            double newMask = easeOut(smoothStart(sloping * erosionParameters.onsetOctave, roundingForOctave * erosionParameters.onsetOctave));
            combiMask = powInv(combiMask, erosionParameters.detail) * newMask;

            ridgeMapFadeTarget = lerp(ridgeMapCombiMask, ridgeMapFadeTarget, gulH);
            double newRidgeMapMask = easeOut(sloping * erosionParameters.onsetRidgeOctave);
            ridgeMapCombiMask = ridgeMapCombiMask * newRidgeMapMask;

            strength *= erosionParameters.gain;
            freq *= erosionParameters.lacunarity;
            roundingMult *= erosionParameters.roundingOctaveMult;
        }

        out.ridgeMap = ridgeMapFadeTarget * (1.0 - ridgeMapCombiMask);
        out.debug = fadeTarget;

        out.heightDelta = h - inH;
        out.slopeDx = sx - inSx;
        out.slopeDz = sz - inSz;
        out.magnitude = magnitude;
    }

    static final class Result {
        double heightDelta;
        double slopeDx;
        double slopeDz;
        double magnitude;
        double ridgeMap;
        double debug;
    }
}