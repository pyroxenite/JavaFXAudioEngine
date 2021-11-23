package main.modules;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import main.Module;
import main.components.Cable;
import main.components.InputPort;
import main.components.OutputPort;
import main.components.Port;
import org.json.simple.JSONObject;
import utilities.ColorTheme;
import utilities.MathFunctions;
import utilities.Point;

import javafx.scene.input.MouseEvent;
import java.util.Arrays;

/**
 * This module produces a constant signal user-customizable between -1.0 and 1.0.
 */
public class KnobModule extends Module {
    private float value = 0;
    private boolean valueDragStarted = false;

    private Point knobPosition;
    private double knobDiameter;
    private String previousName = "Knob";

    private boolean centeredValue = true;

    public KnobModule() {
        super("Knob");

        addOutput("Output").setFrameGenerator(frameLength -> {
            float[] frame = new float[frameLength];
            Arrays.fill(frame, value);
            return frame;
        });
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    protected void updateGeometry() {
        width = 100;
        height = 100;
        if (outputs.size() == 1)
            outputs.get(0).setPosition(width - 11, 26 + (height-26)/2);

        knobDiameter = width/2;
        knobPosition = new Point(width/2 - knobDiameter/2 - 7, 26 + (height - 26)/2 - knobDiameter /2 + 2);
    }

    @Override
    public void draw(GraphicsContext gc) {
        translate(gc, position.getX(), position.getY());

        drawPaneAndTitleBar(gc);

        gc.setLineWidth(10);
        gc.setLineCap(StrokeLineCap.BUTT);
        gc.setStroke(ColorTheme.MODULE_FILL_1);
        gc.strokeArc(knobPosition.getX(), knobPosition.getY(), knobDiameter, knobDiameter, 230, -280, ArcType.OPEN);
        if (isSelected) {
            gc.setStroke(ColorTheme.MODULE_BORDER_SELECTED);
        } else {
            gc.setStroke(ColorTheme.MODULE_FILL_2);
        }
        if (centeredValue)
            gc.strokeArc(knobPosition.getX(), knobPosition.getY(), knobDiameter, knobDiameter, 90, -140*value, ArcType.OPEN);
        else
            gc.strokeArc(knobPosition.getX(), knobPosition.getY(), knobDiameter, knobDiameter, 230, -280*value/2.0+0.5, ArcType.OPEN);

        if (centeredValue) {
            gc.setLineWidth(1);
            gc.setStroke(ColorTheme.MODULE_BORDER);
            gc.strokeLine(
                    knobPosition.getX() + knobDiameter / 2,
                    knobPosition.getY() - 10 / 2,
                    knobPosition.getX() + knobDiameter / 2,
                    knobPosition.getY() + 10 / 2
            );
        }

        drawPortsOnly(gc);

        translate(gc, -position.getX(), -position.getY());

        drawCables(gc);
    }

    @Override
    public void handleMouseClicked(MouseEvent event) {
        Point mousePosition = new Point((int) event.getX(), (int) event.getY());
        valueDragStarted = false;
        Point relativePosition = mousePosition.copy().subtract(position);
        if (relativePosition.getY() < 26) {
            dragStarted = true;
        } else {
            portUnderMouse = findPortUnderMouse(relativePosition);
            if (portUnderMouse != null) {
                if (portUnderMouse.getClass() == OutputPort.class)
                    temporaryCableReference = new Cable((OutputPort) portUnderMouse, mousePosition);
                else
                    temporaryCableReference = new Cable((InputPort) portUnderMouse, mousePosition);
            } else {
                if (relativePosition.distanceTo(knobPosition) < knobDiameter) {
                    if (event.getClickCount() == 1) {
                        valueDragStarted = true;
                        previousName = name;
                    } else {
                        value = 0;
                    }
                }
            }
        }
    }

    @Override
    public void handleDrag(Point mousePosition, Point mouseDelta) {
        if (dragStarted) {
            setPosition(
                    position.getX() + mouseDelta.getX(),
                    position.getY() + mouseDelta.getY()
            );
        } else if (temporaryCableReference != null && portUnderMouse != null) {
            temporaryCableReference.setLooseEndPosition(mousePosition);
        } else if (valueDragStarted) {
            value += (mouseDelta.getX() - mouseDelta.getY())/300;
            value = Math.max(-1, Math.min(1, value));
            name = String.format("%.2f", value);
        }
    }

    @Override
    public void handleMouseReleased(Point mousePosition, Module moduleUnderMouse) {
        dragStarted = false;
        temporaryCableReference = null;

        if (moduleUnderMouse != null && moduleUnderMouse != this) {
            Point relativePosition = mousePosition.copy().subtract(moduleUnderMouse.getPosition());
            Port externalPortUnderMouse = moduleUnderMouse.findPortUnderMouse(relativePosition);

            if (externalPortUnderMouse != null && portUnderMouse.getClass() != externalPortUnderMouse.getClass()) {
                portUnderMouse.connectTo(externalPortUnderMouse);
            }
        }

        if (valueDragStarted)
            name = previousName;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        obj.put("class", "KnobModule");

        obj.put("uuid", uuid.toString());
        obj.put("x-position", position.getX());
        obj.put("y-position", position.getY());

        obj.put("value", value);

        return obj;
    }

    public static KnobModule fromJSON(JSONObject obj) {
        KnobModule knob = new KnobModule();

        knob.setUUID((String) obj.get("uuid"));
        knob.setPosition((double) obj.get("x-position"), (double) obj.get("y-position"));

        knob.setValue((float) (double) obj.get("value"));

        return knob;
    }
}
