package main.modules;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import main.Module;
import main.components.Cable;
import main.components.InputPort;
import main.components.OutputPort;
import utility.ColorTheme;
import utility.Point;

import java.util.Arrays;

public class KnobModule extends Module {
    private float value = 0.5f;
    private boolean valueDragStarted = false;

    private Point knobPosition;
    private double knobDiameter;

    public KnobModule() {
        super("Knob");
        addOutput("Output");

        outputs.get(0).setSignalProvider((frameLength -> {
            float[] frame = new float[frameLength];
            Arrays.fill(frame, value*2-1);
            return frame;
        }));
    }

    @Override
    protected void updateGeometry() {
        width = 100;
        height = 100;
        if (outputs.size() == 1)
            outputs.get(0).setPosition(width - 11, 26 + (height-26)/2);

        knobDiameter = width/2;
        knobPosition = new Point(width/2 - knobDiameter /2 - 7, 26 + (height - 26)/2 - knobDiameter /2 + 2);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.transform(new Affine(1, 0, this.position.getX(),0, 1, this.position.getY()));

        gc.setFill(ColorTheme.MODULE_BACKGROUND);
        if (isSelected) {
            gc.setLineWidth(2);
            gc.setStroke(ColorTheme.MODULE_BORDER_SELECTED);
        } else {
            gc.setLineWidth(1);
            gc.setStroke(ColorTheme.MODULE_BORDER);
        }
        double borderRadius = 30;
        gc.fillRoundRect(0, 0, width, height, borderRadius, borderRadius);
        gc.strokeRoundRect(0, 0, width, height, borderRadius, borderRadius);
        gc.setFill(ColorTheme.TEXT_NORMAL);
        gc.setTextAlign(TextAlignment.CENTER);
        String title = name;
        gc.fillText(name, width/2, 18, width);

        gc.setStroke(ColorTheme.MODULE_BORDER);
        gc.setLineWidth(1);
        if (isSelected) {
            gc.strokeLine(1, 26, width-1, 26);
        } else {
            gc.strokeLine(0, 26, width, 26);
        }

        gc.setLineWidth(10);
        gc.setLineCap(StrokeLineCap.BUTT);
        gc.setStroke(ColorTheme.MODULE_FILL_1);
        gc.strokeArc(knobPosition.getX(), knobPosition.getY(), knobDiameter, knobDiameter, 230, -280, ArcType.OPEN);
        gc.setStroke(ColorTheme.MODULE_FILL_2);
        gc.strokeArc(knobPosition.getX(), knobPosition.getY(), knobDiameter, knobDiameter, 230, -280*value, ArcType.OPEN);

        getOutput(0).draw(gc);

        gc.transform(new Affine(1, 0, -this.position.getX(),0, 1, -this.position.getY()));

        if (temporaryCableReference != null)
            temporaryCableReference.draw(gc);
    }

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
                }
            }
        }
    }

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
        }
    }
}
