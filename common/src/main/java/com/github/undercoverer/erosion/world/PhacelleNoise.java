package com.github.undercoverer.erosion.world;

/**
 * Phacelle Noise function copyright (c) 2025 Rune Skovbo Johansen
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Java port of the GLSL original from the "Advanced Terrain Erosion Filter" by runevision on Shadertoy
 */
public class PhacelleNoise {

    static final double TAU = 6.28318530717959;

    /**
     * The Simple Phacelle Noise function produces a stripe pattern aligned with the input vector.
     * The name Phacelle is a portmanteau of phase and cell, since the function produces a phase by
     * interpolating cosine and sine waves from multiple cells.
     *
     * @param seed              the world seed (mixed into the cell hash)
     * @param px/py             is the input point being evaluated
     * @param normDirX/normDirY is the direction of the stripes at this point. It must be a normalized vector.
     * @param freq              is the frequency of the stripes within each cell. It's best to keep it close to 1.0, as high values will produce distortions and other artifacts.
     * @param offset            is the phase offset of the stripes, where 1.0 is a full cycle.
     * @param normalization     is the degree of normalization applied, between 0 and 1. With e.g. a value of 0.4, raw output with a magnitude below 0.6 won't get fully normalized to a magnitude of 1.0.
     * @param randOut           scratch buffer (length >= 2), reused for the per-cell hash to avoid allocation
     * @param out               result buffer (length >= 4): [cos, sin, sideDirX, sideDirY]
     */
    static void sample(long seed, double px, double py, double normDirX, double normDirY, double freq, double offset, double normalization, double[] randOut, double[] out) {
        // Get a vector orthogonal to the input direction, with a magnitude proportional to the frequency of the stripes.
        double sideDirX = normDirY * -freq * TAU;
        double sideDirY = normDirX * freq * TAU;
        offset *= TAU;

        // Iterate over 4x4 cells, calculating a stripe pattern for each and blending between them.
        // pInt is the integer part of the current coordinate p, pFrac is the remainder.
        //
        // o   o   o   o
        //
        // o   o   o   o
        //       p
        // o   i   o   o
        //
        // o   o   o   o
        //
        // p: current coordinate    i: integer part of p    o: grid points for 4x4 cells
        //
        double pIntX = Math.floor(px), pIntY = Math.floor(py);
        double pFracX = px - pIntX, pFracY = py - pIntY;
        double phaseX = 0, phaseY = 0;
        double weightSum = 0;

        for (int i = -1; i <= 2; i++) {
            for (int j = -1; j <= 2; j++) {
                // Calculate a vector representing the input point relative to this cell point:
                ErosionNoise.hash2Into(seed, pIntX + i, pIntY + j, randOut);
                double vx = pFracX - i - randOut[0] * 0.5;
                double vy = pFracY - j - randOut[1] * 0.5;

                // Bell-shaped weight function which is 1 at dist 0 and nearly 0 at dist 1.5. Due to the random offsets
                // of up to 0.5, the closest a cell point not in the 4x4 grid can be to the current point p is 1.5 units away.
                double sqrDist = vx * vx + vy * vy;

                // Subtract 0.01111 to make the function actually 0 at distance 1.5, which avoids some (very subtle) grid line artefacts.
                double weight = Math.max(0.0, Math.exp(-sqrDist * 2.0) - 0.01111);

                // Keep track of the total sum of weights.
                weightSum += weight;

                // The waveInput is a gradient which increases in value along sideDir. Its rate of
                // change is the freq times tau, due to the multiplier pre-applied to sideDir.
                double waveInput = vx * sideDirX + vy * sideDirY + offset;

                // Add this cell's cosine and sine wave contributions to the interpolated value.
                phaseX += Math.cos(waveInput) * weight;
                phaseY += Math.sin(waveInput) * weight;
            }
        }

        // Get the raw interpolated values.
        double ix = phaseX / weightSum, iy = phaseY / weightSum;
        // Interpret the value as a vector whose length represents the magnitude of both waves.
        double magnitude = Math.sqrt(ix * ix + iy * iy);
        // Apply a lower threshold to show small magnitudes we're going to fully normalize.
        magnitude = Math.max(1.0 - normalization, magnitude);
        // Write a vector containing the normalized cosine and sine waves, as well as the direction vector, which can
        // be multiplied onto the sine to get the derivatives of the cosine.
        out[0] = ix / magnitude;
        out[1] = iy / magnitude;
        out[2] = sideDirX;
        out[3] = sideDirY;
    }
}