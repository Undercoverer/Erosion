package com.github.undercoverer.erosion.world;

// Don't ask me I don't know
public final class ErosionNoise {

    private static long mix64(long z) {
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }

    private static long hashLong(long seed, long salt, double x, double y) {
        long h = seed ^ salt;
        h ^= mix64(Double.doubleToRawLongBits(x) + 0x9E3779B97F4A7C15L);
        h = mix64(h);
        h ^= mix64(Double.doubleToRawLongBits(y) + 0xC2B2AE3D27D4EB4FL);
        return mix64(h);
    }

    private static double toSignedUnit(long h) {
        return ((h >>> 11) * 0x1.0p-53) * 2.0 - 1.0;
    }

    static void hash2Into(long seed, double x, double y, double[] out) {
        out[0] = toSignedUnit(hashLong(seed, 0xA24BAED4963EE407L, x, y));
        out[1] = toSignedUnit(hashLong(seed, 0x9FB21C651E98DF25L, x, y));
    }


    static void noised(long seed, double x, double y, double[] out) {
        double ix = Math.floor(x), iy = Math.floor(y);
        double fx = x - ix, fy = y - iy;

        double ux = fx * fx * fx * (fx * (fx * 6.0 - 15.0) + 10.0);
        double uy = fy * fy * fy * (fy * (fy * 6.0 - 15.0) + 10.0);
        double dux = 30.0 * fx * fx * (fx * (fx - 2.0) + 1.0);
        double duy = 30.0 * fy * fy * (fy * (fy - 2.0) + 1.0);

        double gax = toSignedUnit(hashLong(seed, 0xA24BAED4963EE407L, ix, iy));
        double gay = toSignedUnit(hashLong(seed, 0x9FB21C651E98DF25L, ix, iy));
        double gbx = toSignedUnit(hashLong(seed, 0xA24BAED4963EE407L, ix + 1.0, iy));
        double gby = toSignedUnit(hashLong(seed, 0x9FB21C651E98DF25L, ix + 1.0, iy));
        double gcx = toSignedUnit(hashLong(seed, 0xA24BAED4963EE407L, ix, iy + 1.0));
        double gcy = toSignedUnit(hashLong(seed, 0x9FB21C651E98DF25L, ix, iy + 1.0));
        double gdx = toSignedUnit(hashLong(seed, 0xA24BAED4963EE407L, ix + 1.0, iy + 1.0));
        double gdy = toSignedUnit(hashLong(seed, 0x9FB21C651E98DF25L, ix + 1.0, iy + 1.0));

        double va = gax * fx + gay * fy;
        double vb = gbx * (fx - 1.0) + gby * fy;
        double vc = gcx * fx + gcy * (fy - 1.0);
        double vd = gdx * (fx - 1.0) + gdy * (fy - 1.0);

        double k4 = va - vb - vc + vd;

        double value = va + ux * (vb - va) + uy * (vc - va) + ux * uy * k4;

        double gix = gax + ux * (gbx - gax) + uy * (gcx - gax) + ux * uy * (gax - gbx - gcx + gdx);
        double giy = gay + ux * (gby - gay) + uy * (gcy - gay) + ux * uy * (gay - gby - gcy + gdy);

        out[0] = value;
        out[1] = gix + dux * (uy * k4 + (vb - va));
        out[2] = giy + duy * (ux * k4 + (vc - va));
    }

    static double[] fractalNoise(long seed, double px, double py, double freq,
                                 int octaves, double lacunarity, double gain, double[] n) {
        double nx = 0, ny = 0, nz = 0;
        double nf = freq;
        double na = 1.0;
        for (int i = 0; i < octaves; i++) {
            noised(seed, px * nf, py * nf, n);
            nx += n[0] * na;
            ny += n[1] * na * nf;
            nz += n[2] * na * nf;

            na *= gain;
            nf *= lacunarity;
        }
        n[0] = nx;
        n[1] = ny;
        n[2] = nz;
        return n;
    }
}