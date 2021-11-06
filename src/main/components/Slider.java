package main.components;

import javafx.scene.canvas.GraphicsContext;
import utility.ColorTheme;

public class Slider {
    private int width;
    public static int height = 20;
    private double value = 0.8;
    private int knobWidth = 10;
    private double level = 0;
    private boolean showLevel = false;

    public Slider(int length) {
        this.width = length;
    }

    public Slider(int length, boolean showLevel) {
        this.width = length;
        this.showLevel = showLevel;
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(ColorTheme.MODULE_FILL_1);
        gc.fillRoundRect(0, 0, width, height, 5, 5);
        gc.setFill(ColorTheme.MODULE_TRANSPARENT_FILL);
        gc.fillRoundRect((width-knobWidth)*value, 0,  knobWidth, height, 5, 5);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = Math.max(0, Math.min(1, value));
    }

    public void setLevel(double level) {
        this.level = Math.max(0, Math.min(1, level));
    }

    public void showLevel() {
        showLevel = true;
    }

    public void hideLevel() {
        showLevel = false;
    }
}
