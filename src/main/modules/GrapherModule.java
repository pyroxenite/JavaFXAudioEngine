package main.modules;

import audio.RollingBuffer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;
import main.Module;
import utilities.ColorTheme;

/**
 * This module displays values as they come in, in the form of a rolling graph. One sample per frame is used.
 */
public class GrapherModule extends Module {
    RollingBuffer graphData = new RollingBuffer(100);
    double screenHeight = 150;

    public GrapherModule() {
        super("Grapher");

        addInput("Input").setPosition(11, height - 16);

        addOutput("Passthrough").setFrameGenerator(frameLength -> {
            float[] frame = getInput(0).requestFrame(frameLength);
            graphData.add(frame[0]);
            return frame;
        }).setPosition(width - 11, height - 16);
    }

    @Override
    public void draw(GraphicsContext gc) {
        translate(gc, position.getX(), position.getY());

        drawPaneAndTitleBar(gc);
        drawPortsAndLabels(gc);

        gc.setFill(ColorTheme.MODULE_FILL_1);
        gc.fillRoundRect(5, 26 + 5, width - 10, screenHeight, 5, 5);

        float[] buff = graphData.getBuffer();
        gc.setStroke(ColorTheme.MODULE_FILL_2);
        gc.setLineWidth(3);
        gc.beginPath();
        gc.moveTo(10, 26 + 10 + screenHeight/2 - (screenHeight-10)/2*buff[0]);
        for (int i=1; i<buff.length; i++) {
            double t = (double) i/(buff.length - 1);
            gc.lineTo(10 + t*(width - 20), 26 + 10 + screenHeight/2 - (screenHeight-10)/2*buff[i]);
        }
        gc.stroke();

        //dutyCycleKnob.draw(gc, isSelected);
        //tempoKnob.draw(gc, isSelected);

        gc.transform(new Affine(1, 0, -this.position.getX(), 0, 1, -this.position.getY()));

        drawCables(gc);
    }

    @Override
    protected void updateGeometry() {
        width = 300;
        height = (int) (screenHeight + 22 + 26 + 10);
    }
}
