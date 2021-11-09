package main.components;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.TextAlignment;
import utilities.ColorTheme;
import utilities.Point;

public class Knob {
    private String name = "";
    private double min;
    private double max;
    private String unit;
    private double value = 0.5; // 0 to 1;
    private double knobDiameter;
    private Point position = new Point();

    public Knob(String name, double knobDiameter, double min, double max, double initial, String unit) {
        this.name = name;
        this.max = max;
        this.min = min;
        this.knobDiameter = knobDiameter;
        this.unit = unit;
        this.value = (initial - min) / (max - min);
    }

    public Knob(String name, double knobDiameter, double min, double max, double initial) {
        this.name = name;
        this.max = max;
        this.min = min;
        this.knobDiameter = knobDiameter;
        this.value = (initial - min) / (max - min);
    }

    public double getValue() {
        return min + value*(max-min);
    }

    public String getTextValue() {
        return String.format("%.2f", getValue()) + unit;
    }

    public void addValue(double value) {
        this.value = Math.max(0, Math.min(1, this.value + value));
    }

    public void setValue(double value) {
        this.value = Math.max(0, Math.min(1, this.value + value));
    }

    public void draw(GraphicsContext gc, boolean isSelected) {
        gc.setLineWidth(10);
        gc.setLineCap(StrokeLineCap.BUTT);
        gc.setStroke(ColorTheme.MODULE_FILL_1);
        gc.strokeArc(-knobDiameter/2 + position.getX(), -knobDiameter/2 + position.getY(), knobDiameter, knobDiameter, 230, -280, ArcType.OPEN);
        if (isSelected) {
            gc.setStroke(ColorTheme.MODULE_BORDER_SELECTED);
        } else {
            gc.setStroke(ColorTheme.MODULE_FILL_2);
        }
        gc.strokeArc(-knobDiameter/2 + position.getX(), -knobDiameter/2 + position.getY(), knobDiameter, knobDiameter, 230, -280*value, ArcType.OPEN);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(ColorTheme.TEXT_NORMAL);
        gc.fillText(name, position.getX(), position.getY() + knobDiameter/2 + 16);
    }

    public Knob setPosition(double x, double y) {
        position.set(x, y);
        return this;
    }
}
