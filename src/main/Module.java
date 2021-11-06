package main;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import utility.ColorTheme;
import utility.Point;
import main.components.Cable;
import main.components.InputPort;
import main.components.OutputPort;
import main.components.Port;

import java.util.ArrayList;

public class Module {
    protected String name;

    protected Point position = new Point(0, 0);
    protected int width = 150;
    protected int height = 200;
    protected ArrayList<InputPort> inputs = new ArrayList<>();
    protected ArrayList<OutputPort> outputs = new ArrayList<>();

    protected boolean dragStarted = false;
    protected boolean isSelected = false;
    protected Port portUnderMouse = null;

    protected Cable temporaryCableReference = null;

    public Module(String name) {
        this.name = name;
        updateGeometry();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(double x, double y) {
        this.position.setX(x);
        this.position.setY(y);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ArrayList<InputPort> getInputs() {
        return inputs;
    }

    public ArrayList<OutputPort> getOutputs() {
        return outputs;
    }

    public InputPort getInput(int i) {
        return inputs.get(i);
    }

    public OutputPort getOutput(int i) {
        return outputs.get(i);
    }

    public void addInput(String name) {
        inputs.add(new InputPort(name, this));
        updateGeometry();
    }

    public void addOutput(String name) {
        outputs.add(new OutputPort(name, this));
        updateGeometry();
    }

    public Rectangle2D getBoundingBox() {
        return new Rectangle2D(
                position.getX(),
                position.getY(),
                width,
                height
        );
    }

    protected void updateGeometry() {
        int textLineHeight = 22;

        int maxInputLabelWidth = 15;
        for (Port input: inputs) {
            maxInputLabelWidth = Math.max(
                    maxInputLabelWidth,
                    (int) new Text(input.getName()).getBoundsInLocal().getWidth() + 22
            );
        }
        int maxOutputLabelWidth = 15;
        for (Port output: outputs) {
            maxOutputLabelWidth = Math.max(
                    maxOutputLabelWidth,
                    (int) new Text(output.getName()).getBoundsInLocal().getWidth() + 22
            );
        }
        this.width = maxInputLabelWidth + maxOutputLabelWidth;
        if (maxInputLabelWidth != 15 && maxOutputLabelWidth != 15)
            this.width += 15;

        int titleWidth = (int) new Text(name).getBoundsInLocal().getWidth() + 40;
        width = Math.max(titleWidth, width);

        height = (int) ((Math.max(outputs.size(), inputs.size()) + 1) * textLineHeight + 14);
        height = Math.max(40, height);

        int x = 11;
        int y = (int) ((Math.max(0, outputs.size() - inputs.size())) * textLineHeight / 2 + 20);
        for (Port input: inputs) {
            y += textLineHeight;
            input.setPosition(x, y);
        }

        x = this.width - 11;
        y = (int) ((Math.max(0, inputs.size() - outputs.size())) * textLineHeight / 2 + 20);
        for (Port output: outputs) {
            y += textLineHeight;
            output.setPosition(x, y);
        }
    }

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
        gc.fillText(name, width/2, 18, width);

        gc.setStroke(ColorTheme.MODULE_BORDER);
        gc.setLineWidth(1);
        if (isSelected) {
            gc.strokeLine(1, 26, width-1, 26);
        } else {
            gc.strokeLine(0, 26, width, 26);
        }

        gc.setTextAlign(TextAlignment.LEFT);
        for (InputPort inputPort: inputs) {
            inputPort.draw(gc);
            gc.setFill(ColorTheme.TEXT_NORMAL);
            gc.fillText(inputPort.getName(), inputPort.getPosition().getX()+10, inputPort.getPosition().getY()+4);
        }

        gc.setTextAlign(TextAlignment.RIGHT);
        for (OutputPort outputPort: outputs) {
            outputPort.draw(gc);
            gc.setFill(ColorTheme.TEXT_NORMAL);
            gc.fillText(outputPort.getName(), outputPort.getPosition().getX()-10, outputPort.getPosition().getY()+4);
        }

        gc.transform(new Affine(1, 0, -this.position.getX(),0, 1, -this.position.getY()));

        if (temporaryCableReference != null)
            temporaryCableReference.draw(gc);

        for (InputPort inputPort: inputs) {
            Cable cable = inputPort.getCable();
            if (cable != null)
                cable.draw(gc);
        }
    }

    protected Port findPortUnderMouse(Point relativeMousePosition) {
        for (Port inputPort: inputs) {
            if (inputPort.getPosition().distanceTo(relativeMousePosition) < 7) {
                return inputPort;
            }
        }
        for (Port outputPort: outputs) {
            if (outputPort.getPosition().distanceTo(relativeMousePosition) < 7) {
                return outputPort;
            }
        }
        return null;
    }

    public void handleMouseClicked(Point mousePosition) {
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
            }
        }
    }

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
    }

    public void handleDrag(Point mousePosition, Point mouseDelta) {
        if (dragStarted) {
            setPosition(
                    position.getX() + mouseDelta.getX(),
                    position.getY() + mouseDelta.getY()
            );
        } else if (temporaryCableReference != null && portUnderMouse != null) {
            temporaryCableReference.setLooseEndPosition(mousePosition);
        }
    }

    public void select() {
        isSelected = true;
    }

    public void deselect() {
        isSelected = false;
    }
}
