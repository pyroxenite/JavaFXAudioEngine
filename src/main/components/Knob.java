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
    private String unit = "";
    private double value = 0.5; // 0 to 1;
    private double knobDiameter;
    private Point position = new Point();

    public static final int NAME_AS_LABEL = 0;
    public static final int VALUE_AS_LABEL = 1;
    private int labelMode = NAME_AS_LABEL;

    public static final int LINEAR = 0;
    public static final int EXPONENTIAL = 1;
    private int valueMapMode = LINEAR;

    public Knob(String name, double knobDiameter, double min, double max, double initial, String unit) {
        this.name = name;
        this.max = max;
        this.min = min;
        this.knobDiameter = knobDiameter;
        this.unit = unit;
        this.value = initial;
    }

    public Knob(String name, double knobDiameter, double min, double max, double initial) {
        this.name = name;
        this.max = max;
        this.min = min;
        this.knobDiameter = knobDiameter;
        this.value = initial;
    }

    public double getValue() {
        if (valueMapMode == LINEAR)
            return min + value*(max-min);
        else {
            double s = 6; // steepness
            double expValue = (Math.exp(s*value) - Math.exp(s*0))/(Math.exp(s) - Math.exp(0));
            return min + expValue*(max-min);
        }
    }

    public String getTextValue() {
        if (getValue() < 10) {
            return String.format("%.2f", getValue()) + unit;
        } else {
            return String.format("%.0f", getValue()) + unit;
        }
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
        gc.fillText(labelMode==NAME_AS_LABEL?name:getTextValue(), position.getX(), position.getY() + knobDiameter/2 + 16);
    }

    public Knob setPosition(double x, double y) {
        position.set(x, y);
        return this;
    }

    public Knob displayValue() {
        labelMode = VALUE_AS_LABEL;
        return this;
    }

    public Knob displayName() {
        labelMode = NAME_AS_LABEL;
        return this;
    }

    public Knob setMapMode(int mode) {
        valueMapMode = mode;
        return this;
    }
}
