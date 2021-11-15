package main.components;

import javafx.scene.canvas.GraphicsContext;
import main.interfaces.Drawable;
import main.interfaces.FrameGenerator;
import main.Module;
import utilities.ColorTheme;
import utilities.Point;

/**
 * A port either receives or transmits audio data and acts as a node in the audio pipeline. In addition, it acts as a
 * UI element.
 */
public class Port implements FrameGenerator, Drawable {
    private Point position = new Point(0, 0);
    private String name;
    private Module parent;
    private FrameGenerator frameGenerator = ZERO_FRAME_GENERATOR;

    final protected static FrameGenerator ZERO_FRAME_GENERATOR = (frameLength) -> { return new float[frameLength]; };

    public Port(String name, Module parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public Point getPosition() {
        return position;
    }

    public Point getAbsolutePosition() {
        return position.copy().add(parent.getPosition());
    }

    public Module getParent() {
        return parent;
    }

    public Port setPosition(double x, double y) {
        position.set(x, y);
        return this;
    }

    public Port setFrameGenerator(FrameGenerator frameGenerator) {
        this.frameGenerator = frameGenerator;
        return this;
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(ColorTheme.CABLE_STROKE);
        gc.fillOval(position.getX()-5, position.getY()-5, 10, 10);
        gc.setLineWidth(1);
        gc.setStroke(ColorTheme.PORT_STROKE);
        gc.strokeOval(position.getX()-5, position.getY()-5, 10, 10);
    }

    public float[] requestFrame(int frameLength) {
        return frameGenerator.requestFrame(frameLength);
    }

    public void connectTo(Port port) {

    }
}