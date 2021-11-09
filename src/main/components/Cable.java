package main.components;

import javafx.scene.canvas.GraphicsContext;
import utilities.ColorTheme;
import utilities.Point;

public class Cable {
    private OutputPort sourcePort;
    private InputPort targetPort;
    private Point looseEndPosition; // used during drag operation
    private boolean isConnectedToSource = false;
    private boolean isConnectedToTarget = false;

    public Cable(OutputPort sourcePort, InputPort targetPort) {
        this.sourcePort = sourcePort;
        isConnectedToSource = true;
        this.targetPort = targetPort;
        isConnectedToTarget = true;
    }

    public Cable(OutputPort sourcePort, Point looseEndPosition) {
        this.sourcePort = sourcePort;
        isConnectedToSource = true;
        this.looseEndPosition = looseEndPosition;
        isConnectedToTarget = false;
    }

    public Cable(InputPort targetPort, Point looseEndPosition) {
        this.targetPort = targetPort;
        isConnectedToTarget = true;
        this.looseEndPosition = looseEndPosition;
        isConnectedToSource = false;
    }

    public void setLooseEndPosition(Point looseEndPosition) {
        this.looseEndPosition = looseEndPosition;
    }

    public void setSourcePort(OutputPort sourcePort) {
        this.sourcePort = sourcePort;
    }

    public void setTargetPort(InputPort targetPort) {
        this.targetPort = targetPort;
    }

    public void draw(GraphicsContext gc) {
        Point startPoint;
        Point endPoint;
        if (isConnectedToSource && isConnectedToTarget) {
            startPoint = sourcePort.getAbsolutePosition().add(new Point(5, 0));
            endPoint = targetPort.getAbsolutePosition().add(new Point(-5, 0));
        } else if (isConnectedToSource) {
            startPoint = sourcePort.getAbsolutePosition().add(new Point(5, 0));
            endPoint = looseEndPosition;
        } else if (isConnectedToTarget) {
            startPoint = looseEndPosition;
            endPoint = targetPort.getAbsolutePosition().add(new Point(-5, 0));
        } else {
            return;
        }

        double tangent = startPoint.distanceTo(endPoint) / 3;

        gc.setStroke(ColorTheme.CABLE_STROKE);
        gc.setLineWidth(2);

        gc.beginPath();
        gc.moveTo(startPoint.getX(), startPoint.getY());
        gc.bezierCurveTo(
                startPoint.getX() + tangent, startPoint.getY(),
                endPoint.getX() - tangent, endPoint.getY(),
                endPoint.getX(), endPoint.getY()
        );
        gc.stroke();
    }

    public boolean isConnectedToSource() {
        return isConnectedToSource;
    }

    public boolean isConnectedToTarget() {
        return isConnectedToTarget;
    }

    public Port getSource() {
        return sourcePort;
    }

    public Port getTarget() {
        return targetPort;
    }
}