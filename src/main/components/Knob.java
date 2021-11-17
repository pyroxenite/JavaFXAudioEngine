package main.components;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.TextAlignment;
import main.interfaces.Drawable;
import utilities.ColorTheme;
import utilities.MathFunctions;
import utilities.Point;

/**
 * A knob is a circular UI element that users interact with by dragging. It stores a customizable bounded value.
 */
public class Knob implements Drawable {
    private String name = "";
    private double min;
    private double max;
    private String unit = "";
    private double value = 0.5; // 0 to 1;
    private double knobDiameter;
    private Point position = new Point();
    private double defaultValue = 0.5;

    public void resetValue() {
        value = defaultValue;
    }

    public enum LabelMode { NAME_AS_LABEL, VALUE_AS_LABEL; }
    private LabelMode labelMode = LabelMode.NAME_AS_LABEL;

    public enum MapMode { LINEAR, EXPONENTIAL }
    private MapMode valueMapMode = MapMode.LINEAR;

    private boolean valueDrawnCentered = false;

    private boolean isSelected = false;

    public Knob(String name, double knobDiameter, double min, double max, double initial, String unit) {
        this(name, knobDiameter, min, max, initial);
        this.unit = unit;
    }

    public Knob(String name, double knobDiameter, double min, double max, double initial) {
        this.name = name;
        this.max = max;
        this.min = min;
        this.knobDiameter = knobDiameter;
        this.value = initial;
        this.defaultValue = initial;
    }

    public double getValue() {
        if (valueMapMode == MapMode.LINEAR)
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

    public void draw(GraphicsContext gc) {
        gc.setLineWidth(10);
        gc.setLineCap(StrokeLineCap.BUTT);
        gc.setStroke(ColorTheme.MODULE_FILL_1);
        gc.strokeArc(-knobDiameter/2 + position.getX(), -knobDiameter/2 + position.getY(), knobDiameter, knobDiameter, 230, -280, ArcType.OPEN);
        if (isSelected) {
            gc.setStroke(ColorTheme.MODULE_BORDER_SELECTED);
        } else {
            gc.setStroke(ColorTheme.MODULE_FILL_2);
        }
        if (valueDrawnCentered)
            gc.strokeArc(
                    -knobDiameter/2 + position.getX(), -knobDiameter/2 + position.getY(),
                    knobDiameter, knobDiameter,
                    90, -140* MathFunctions.lerp(-1, 1, value),
                    ArcType.OPEN
            );
        else
            gc.strokeArc(
                    -knobDiameter/2 + position.getX(), -knobDiameter/2 + position.getY(),
                    knobDiameter, knobDiameter,
                    230, -280*value,
                    ArcType.OPEN
            );

        if (valueDrawnCentered) {
            gc.setLineWidth(1);
            gc.setStroke(ColorTheme.MODULE_BORDER);
            gc.strokeLine(
                    position.getX(), position.getY() - knobDiameter/2 - 10 / 2,
                    position.getX(), position.getY() - knobDiameter/2 + 10 / 2
            );
        }
        //gc.strokeArc(-knobDiameter/2 + position.getX(), -knobDiameter/2 + position.getY(), knobDiameter, knobDiameter, 230, -280*value, ArcType.OPEN);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(ColorTheme.TEXT_NORMAL);
        gc.fillText(labelMode== LabelMode.NAME_AS_LABEL?name:getTextValue(), position.getX(), position.getY() + knobDiameter/2 + 16);
    }

    public Knob setPosition(double x, double y) {
        position.set(x, y);
        return this;
    }

    /**
     * The label drawn under the knob will display the knob's value.
     */
    public Knob displayValue() {
        labelMode = LabelMode.VALUE_AS_LABEL;
        return this;
    }

    /**
     * The label drawn under the knob will display the knob's name.
     */
    public Knob displayName() {
        labelMode = LabelMode.NAME_AS_LABEL;
        return this;
    }

    /**
     * By default, a knob will linearly interpolate the minimum and maximum values based on how much it is turned. This
     * function allows for this interpolation to be exponential (which makes it easier to chose small values precisely
     * without reducing the range of the knob).
     * @param mapMode Either Knob.MapMode.LINEAR or Knob.MapMode.EXPONENTIAL.
     * @return Returns the knob object to allow for chaining.
     */
    public Knob setMapMode(MapMode mapMode) {
        valueMapMode = mapMode;
        return this;
    }

    public Knob setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        return this;
    }

    public Knob drawValueCentered() {
        valueDrawnCentered = true;
        return this;
    }
}


