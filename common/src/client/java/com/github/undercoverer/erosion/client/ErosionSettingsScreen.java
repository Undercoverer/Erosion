package com.github.undercoverer.erosion.client;

import com.github.undercoverer.erosion.world.ErosionParameters;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.text.DecimalFormat;
import java.util.function.Consumer;


public final class ErosionSettingsScreen extends Screen {
    private static final int SLIDER_WIDTH = 170;
    private static final int SLIDER_HEIGHT = 20;
    private static final int SLIDER_SPACING = 24;
    private static final int COLUMN_GAP = 14;
    private static final int BUTTON_WIDTH = 90;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROWS = 9;

    private final Screen parentScreen;
    private ErosionParameters.Builder builder;

    public ErosionSettingsScreen(Screen parentScreen) {
        super(Component.translatable("erosion.erosion_settings.title"));
        this.parentScreen = parentScreen;
        this.builder = ErosionParameters.toBuilder(ErosionParameters.defaults());
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int col1X = centerX - (3 * SLIDER_WIDTH + 2 * COLUMN_GAP) / 2;
        int col2X = col1X + SLIDER_WIDTH + COLUMN_GAP;
        int col3X = col2X + SLIDER_WIDTH + COLUMN_GAP;
        int topY = 42;
        int bottomY = topY + ROWS * SLIDER_SPACING;

        addCenteredTextWidget(this.title, centerX, 14, 0xFFFFFF);
        addCenteredTextWidget(Component.literal("Stacked-gully erosion terrain"),
                centerX, 26, 0xAAAAAA);

        // Column 1: base terrain shape / Minecraft mapping.
        addSlider(col1X, topY + 0 * SLIDER_SPACING, "World Scale (blocks/unit)",
                128, 8192, builderSnapshot().blocksPerUnit, "0",
                v -> builder.blocksPerUnit(v),
                "Extra horizontal scaling");
        addSlider(col1X, topY + SLIDER_SPACING, "Height Amplitude",
                0.01, 1, builderSnapshot().heightAmp, "0.000",
                v -> builder.heightAmp(v),
                "The vertical scale (amplitude) of the terrain noise function.");
        addSlider(col1X, topY + 2 * SLIDER_SPACING, "Height Frequency",
                0.01, 8.0, builderSnapshot().heightFrequency, "0.00",
                v -> builder.heightFrequency(v),
                "The inverse horizontal scale of the terrain noise function.");
        addSlider(col1X, topY + 3 * SLIDER_SPACING, "Height Octaves",
                1, 8, builderSnapshot().heightOctaves, "0",
                v -> builder.heightOctaves((int) Math.round(v)),
                "Control over the noise function octaves, with each successive octave layering smaller bumps onto the terrain.");
        addSlider(col1X, topY + 4 * SLIDER_SPACING, "Height Lacunarity",
                1.0, 4.0, builderSnapshot().heightLacunarity, "0.00",
                v -> builder.heightLacunarity(v),
                "The lacunarity controls the frequency (the inverse horizontal scale) of each octave relative to the last.");
        addSlider(col1X, topY + 5 * SLIDER_SPACING, "Height Gain",
                0.0, 1.0, builderSnapshot().heightGain, "0.00",
                v -> builder.heightGain(v),
                "The gain controls the magnitude (the vertical scale) of each octave relative to the last.");
        addSlider(col1X, topY + 6 * SLIDER_SPACING, "Height Offset",
                -1.0, 1.0, builderSnapshot().heightOffsetValue, "0.00",
                v -> builder.heightOffsetValue(v),
                "An offset value between -1 and 1, where a value of -1 only lowers, while 1 only raises. The offset is proportional to the erosion strength parameter, so if that parameter is the same for the entire terrain, the effect of the height offset will move the entire terrain surface up or down by the same amount.");
        addSlider(col1X, topY + 7 * SLIDER_SPACING, "Height Offset Fade Mix",
                0.0, 1.0, builderSnapshot().heightOffsetFadeMix, "0.00",
                v -> builder.heightOffsetFadeMix(v),
                "A value between 0 and 1 which is the degree to which the offset value is replaced by the negated erosion fade target value. This has the effect of only raising at valleys and only lowering at peaks, which, due to how the erosion filter works, has the effect of largely preserving the minima and maxima of the terrain.");

        // Column 2: erosion octave structure.
        addSlider(col2X, topY + 0 * SLIDER_SPACING, "Erosion Octaves",
                1, 8, builderSnapshot().octaves, "0",
                v -> builder.octaves((int) Math.round(v)),
                "Control over the erosion octaves, with each successive octave layering smaller gullies onto the terrain.");
        addSlider(col2X, topY + 1 * SLIDER_SPACING, "Erosion Strength",
                0.0, 1.0, builderSnapshot().erosionStrength, "0.00",
                v -> builder.erosionStrength(v),
                "The strength of the erosion effect, affecting the magnitude of all octaves, and indirectly affecting the directions of the gullies as a result.");
        addSlider(col2X, topY + 2 * SLIDER_SPACING, "Erosion Scale",
                0.02, 1, builderSnapshot().erosionScale, "0.00",
                v -> builder.erosionScale(v),
                "The scale of the erosion effect, affecting it both horizontally and vertically.");
        addSlider(col2X, topY + 3 * SLIDER_SPACING, "Erosion Detail",
                0.5, 3.0, builderSnapshot().detail, "0.00",
                v -> builder.detail(v),
                "The overall detail of the erosion. Lower values restrict the effect of higher frequency gullies to steeper slopes.");
        addSlider(col2X, topY + 4 * SLIDER_SPACING, "Erosion Lacunarity",
                1.0, 4.0, builderSnapshot().lacunarity, "0.00",
                v -> builder.lacunarity(v),
                "The lacunarity controls the frequency (the inverse horizontal scale) of each octave relative to the last.");
        addSlider(col2X, topY + 5 * SLIDER_SPACING, "Erosion Gain",
                0.0, 1.0, builderSnapshot().gain, "0.00",
                v -> builder.gain(v),
                "The gain controls the magnitude (the vertical scale) of each octave relative to the last.");
        addSlider(col2X, topY + 6 * SLIDER_SPACING, "Cell Scale",
                0.1, 3.0, builderSnapshot().cellScale, "0.00",
                v -> builder.cellScale(v),
                "Gullies are based on stripes within Voronoi-like cells in the Phacelle noise function. The cell scale parameter controls the sizes of the cells relative to the overall erosion scale, while keeping the stripe widths unaffected. Values close to 1 usually produce good results. Smaller values produce more grainy gullies while larger values produce longer unbroken gullies, but too large values produce chaotic curved gullies that are not aligned with the slopes. Value changes can cause abrupt changes in output, especially far away from the origin, so this parameter is not well suited for animation or for modulation by other functions.");
        addSlider(col2X, topY + 7 * SLIDER_SPACING, "Normalization",
                0.0, 1.0, builderSnapshot().normalization, "0.00",
                v -> builder.normalization(v),
                "The degree of normalization applied in the Phacelle noise, between 0 and 1. The erosion filter depends on a certain consistency in magnitude of the Phacelle output. However, high values can create loopy results where ridges and creases meet up at a point, which produces unnatural looking results.");

        // Column 3: gully rounding, onset, and slope behavior.
        addSlider(col3X, topY + 0 * SLIDER_SPACING, "Gully Weight",
                0.0, 1.0, builderSnapshot().gullyWeight, "0.00",
                v -> builder.gullyWeight(v),
                "The magnitude of the gullies as a weight value from 0 to 1. A value of 0 can sharpen peaks and valleys but feature virtually no gullies. A value of 1 produces full gullies but may leave peaks and valleys rounded. Adjusting erosion gully weight while inversely adjusting erosion scale can be used to control the sharpness of peaks and valleys while leaving gully magnitudes largely untouched.");
        addSlider(col3X, topY + 1 * SLIDER_SPACING, "Ridge Rounding",
                0.0, 1.0, builderSnapshot().roundingRidge, "0.00",
                v -> builder.roundingRidge(v),
                "Separate rounding control of ridges.");
        addSlider(col3X, topY + 2 * SLIDER_SPACING, "Crease Rounding",
                0.0, 1.0, builderSnapshot().roundingCrease, "0.00",
                v -> builder.roundingCrease(v),
                "Separate rounding control of creases.");
        addSlider(col3X, topY + 3 * SLIDER_SPACING, "Initial Rounding Mult",
                0.0, 1.0, builderSnapshot().roundingInit, "0.00",
                v -> builder.roundingInit(v),
                "Multiplier applied to the initial height function. E.g. if the height function has noise of 5 times lower frequency than the largest gullies, a value of 0.2 can compensate for that.");
        addSlider(col3X, topY + 4 * SLIDER_SPACING, "Octave Rounding Mult",
                0.0, 4.0, builderSnapshot().roundingOctaveMult, "0.00",
                v -> builder.roundingOctaveMult(v),
                "Multiplier applied to each subsequent gully octave after the first. Setting it to the same value as the erosion lacunarity will produce consistent rounding of all octaves.");
        addSlider(col3X, topY + 5 * SLIDER_SPACING, "Initial Onset",
                0.0, 3.0, builderSnapshot().onsetInit, "0.00",
                v -> builder.onsetInit(v),
                "Onset used on the initial height function.");
        addSlider(col3X, topY + 6 * SLIDER_SPACING, "Octave Onset",
                0.0, 3.0, builderSnapshot().onsetOctave, "0.00",
                v -> builder.onsetOctave(v),
                "Onset used on each gully octave.");
        addSlider(col3X, topY + 7 * SLIDER_SPACING, "Ridge Onset (Initial)",
                0.0, 5.0, builderSnapshot().onsetRidgeInit, "0.00",
                v -> builder.onsetRidgeInit(v),
                "RidgeMap-specific onset used on the initial height function.");
        addSlider(col3X, topY + 8 * SLIDER_SPACING, "Ridge Onset (Octave)",
                0.0, 3.0, builderSnapshot().onsetRidgeOctave, "0.00",
                v -> builder.onsetRidgeOctave(v),
                "RidgeMap-specific onset used on each gully octave.");

        // Overflow from column 1/2 (assumed slope)
        addSlider(col1X, topY + 8 * SLIDER_SPACING, "Assumed Slope",
                0.0, 2.0, builderSnapshot().assumedSlopeValue, "0.00",
                v -> builder.assumedSlopeValue(v),
                "An assumed slope value to override the actual slope.");
        addSlider(col2X, topY + 8 * SLIDER_SPACING, "Assumed Slope Mix",
                0.0, 1.0, builderSnapshot().assumedSlopeAmount, "0.00",
                v -> builder.assumedSlopeAmount(v),
                "The amount (from 0 to 1) to override the actual slope.");

        int buttonsY = bottomY + 10;
        this.addRenderableWidget(Button.builder(Component.literal("Reset"), b -> onResetPressed())
                .bounds(centerX - BUTTON_WIDTH - 105, buttonsY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onDonePressed())
                .bounds(centerX + 15, buttonsY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> onClose())
                .bounds(centerX + 110, buttonsY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    private ErosionParameters builderSnapshot() {
        return builder.build();
    }

    private void addSlider(int x, int y, String label, double min, double max,
                           double initial, String format, Consumer<Double> applier,
                           String tooltipText) {
        ParamSlider slider = new ParamSlider(x, y, label, min, max, initial, format, applier);
        slider.setTooltip(Tooltip.create(Component.literal(tooltipText)));
        slider.setTooltipDelay(java.time.Duration.ofMillis(300));
        this.addRenderableWidget(slider);
    }

    private void addCenteredTextWidget(Component text, int centerX, int y, int color) {
        int textWidth = this.font.width(text);
        MutableComponent coloredText = text.copy().withStyle(style -> style.withColor(color));
        StringWidget widget = new StringWidget(centerX - textWidth / 2, y, textWidth, 9, coloredText, this.font);
        this.addRenderableWidget(widget);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parentScreen);
        }
    }

    private void onDonePressed() {
        com.github.undercoverer.erosion.world.ErosionSelections.set(builder.build());
        onClose();
    }

    private void onResetPressed() {
        builder = ErosionParameters.toBuilder(ErosionParameters.defaults());
        this.rebuildWidgets();
    }

    /**
     * Slider mapping its 0..1 range linearly onto a parameter range.
     */
    private static final class ParamSlider extends AbstractSliderButton {
        private final String label;
        private final double min, max;
        private final DecimalFormat format;
        private final Consumer<Double> applier;

        ParamSlider(int x, int y, String label, double min, double max,
                    double initial, String formatPattern, Consumer<Double> applier) {
            super(x, y, SLIDER_WIDTH, SLIDER_HEIGHT, CommonComponents.EMPTY,
                    Math.max(0.0, Math.min(1.0, (initial - min) / (max - min))));
            this.label = label;
            this.min = min;
            this.max = max;
            this.format = new DecimalFormat(formatPattern);
            this.applier = applier;
            updateMessage();
        }

        private double mappedValue() {
            return min + this.value * (max - min);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(label + ": " + format.format(mappedValue())));
        }

        @Override
        protected void applyValue() {
            applier.accept(mappedValue());
        }
    }
}
