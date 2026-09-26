package com.github.undercoverer.erosion.world;

import java.util.concurrent.atomic.AtomicReference;

public final class ErosionSelections {
    private static final AtomicReference<ErosionParameters> P = new AtomicReference<>();

    public static void set(ErosionParameters parameters) {
        P.set(parameters);
    }

    public static ErosionParameters consume() {
        return P.getAndSet(null);
    }
}
