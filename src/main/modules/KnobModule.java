package main.modules;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import main.Module;
import main.components.Cable;
import main.components.InputPort;
import main.components.OutputPort;
import main.components.Port;
import utilities.ColorTheme;
import utilities.Point;

import java.util.Arrays;

public class KnobModule extends Module {
    private float value = 0.5f;
    private boolean valueDragStarted = false;

    private Point knobPosition;
    private double knobDiameter;
    private String previousName = "Knob";

    public KnobModule() {
        super("Knob");

        addOutput("Output").setSignalProvider(n -> {
            float[] frame = new float[n];
            Arrays.fill(frame, value*2-1);
            return frame;
        });
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
        gc.strokeArc(knobPosition.getX(), knobPosition.getY(), knobDiameter, knobDiameter, 230, -280*value, ArcType.OPEN);

        drawPortsOnly(gc);

        translate(gc, -position.getX(), -position.getY());

        drawCables(gc);
    }

    @Override
    public void handleMouseClicked(Point mousePosition) {
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
                    valueDragStarted = true;
                    previousName = name;
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
            value += (mouseDelta.getX() - mouseDelta.getY())/500;
            value = Math.max(0, Math.min(1, value));
            name = String.format("%.2f", value*2 - 1);
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
}
