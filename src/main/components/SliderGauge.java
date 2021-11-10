package main.components;

import javafx.scene.canvas.GraphicsContext;
import utilities.ColorTheme;
import utilities.MathFunctions;

public class SliderGauge {
    private int width;
    public static int height = 20;
    private double sliderValue = 0.8;
    private int knobWidth = 20;
    private double gaugeValue = 0;
    private double gaugeDisplayValueSlow = 0;
    private double gaugeDisplayValueFast = 0;
    private boolean showLevel = false;

    public SliderGauge(int length) {
        this.width = length;
    }

    public SliderGauge(int length, boolean showLevel) {
        this.width = length;
        this.showLevel = showLevel;
    }

    public void draw(GraphicsContext gc) {
        updateGaugeDisplayValues();
        gc.setFill(ColorTheme.MODULE_FILL_1);
        gc.fillRoundRect(0, 0, width, height, 5, 5);
        if (gaugeValue == 1)
            gc.setFill(ColorTheme.AUDIO_DIMMED_RED);
        else
            gc.setFill(ColorTheme.AUDIO_DIMMED_GREEN);
        gc.fillRoundRect(0, 0, width*gaugeDisplayValueSlow, height, 5, 5);
        if (gaugeValue == 1)
            gc.setFill(ColorTheme.AUDIO_RED);
        else
            gc.setFill(ColorTheme.AUDIO_GREEN);
        gc.fillRoundRect(0, 0, width*gaugeDisplayValueFast, height, 5, 5);
        gc.setFill(ColorTheme.MODULE_TRANSPARENT_FILL);
        gc.fillRoundRect((width-knobWidth)* sliderValue, 0,  knobWidth, height, 5, 5);
    }

    private void updateGaugeDisplayValues() {
        gaugeDisplayValueFast = MathFunctions.lerp(gaugeDisplayValueFast, gaugeValue, 0.8);
        gaugeDisplayValueSlow = Math.max(gaugeDisplayValueFast, MathFunctions.lerp(gaugeDisplayValueSlow, gaugeValue, 0.02));
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public double getSliderValue() {
        return sliderValue;
    }

    public void setSliderValue(double sliderValue) {
        this.sliderValue = Math.max(0, Math.min(1, sliderValue));
    }

    public void showLevel() {
        showLevel = true;
    }

    public void hideLevel() {
        showLevel = false;
    }

    public SliderGauge setGaugeValue(double gaugeValue) {
        this.gaugeValue = Math.max(0, Math.min(1, gaugeValue));
        return this;
    }
}
