package main.components;

import audio.SignalProvider;
import javafx.scene.canvas.GraphicsContext;
import main.Module;
import utility.ColorTheme;
import utility.Point;

public class Port {
    private Point position = new Point(0, 0);
    private String name;
    private Module parent;
    private SignalProvider signalProvider = zeroSignalProvider;

    final protected static SignalProvider zeroSignalProvider = (frameLength) -> { return new float[frameLength]; };

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

    public void setPosition(int x, int y) {
        position.set(x, y);
    }

    public void setSignalProvider(SignalProvider signalProvider) {
        this.signalProvider = signalProvider;
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(ColorTheme.CABLE_STROKE);
        gc.fillOval(position.getX()-5, position.getY()-5, 10, 10);
        gc.setLineWidth(1);
        gc.setStroke(ColorTheme.PORT_STROKE);
        gc.strokeOval(position.getX()-5, position.getY()-5, 10, 10);
    }

    public float[] requestFrame(int frameLength) {
        return signalProvider.requestFrame(frameLength);
    }

    public void connectTo(Port port) {

    }
}
