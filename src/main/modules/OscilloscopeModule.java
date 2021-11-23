package main.modules;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;
import main.Module;
import org.json.simple.JSONObject;
import utilities.ColorTheme;

/**
 * This module is similar to the grapher module. It displays the incoming signal but also implements a trigger function
 * to display periodic signals more reliably.
 */
public class OscilloscopeModule extends Module {
    float[] graphData = new float[100];
    float[] recordData = new float[100];
    int recordHead = -1;
    double screenHeight = 150;
    float trigger = 0;
    float previousSample = 0;
    int divCounter = 0;
    int downsampleFactor = 10;

    public OscilloscopeModule() {
        super("Oscilloscope");

        addInput("Input").setPosition(11, height - 35);

        addOutput("Passthrough").setFrameGenerator(frameLength -> {
            float[] frame = getInput(0).requestFrame(frameLength);
            if (frame[0] > trigger && previousSample < trigger && recordHead == -1) {
                recordHead = 0;
            } else {
                previousSample = frame[0];
            }
            if (recordHead >= 0 && recordHead < recordData.length) {
                recordData[recordHead] = frame[0];
                recordHead++;
            }
            if (recordHead == recordData.length) {
                graphData = recordData;
                recordHead = -1;
            }
            return frame;
        }).setPosition(width - 11, height - 35);
    }

    @Override
    public void draw(GraphicsContext gc) {
        translate(gc, position.getX(), position.getY());

        drawPaneAndTitleBar(gc);
        drawPortsAndLabels(gc);

        gc.setFill(ColorTheme.MODULE_FILL_1);
        gc.fillRoundRect(5, 26 + 5, width - 10, screenHeight, 5, 5);

        gc.setStroke(ColorTheme.MODULE_FILL_2);
        gc.setLineWidth(3);
        gc.beginPath();
        gc.moveTo(10, 26 + 5 + screenHeight/2 - (screenHeight-10)/2*graphData[0]);
        for (int i=1; i<graphData.length; i++) {
            double t = (double) i/(graphData.length - 1);
            gc.lineTo(10 + t*(width - 20), 26 + 5 + screenHeight/2 - (screenHeight-10)/2*graphData[i]);
        }
        gc.stroke();

        gc.transform(new Affine(1, 0, -this.position.getX(), 0, 1, -this.position.getY()));

        drawCables(gc);
    }

    @Override
    protected void updateGeometry() {
        width = 300;
        height = (int) (screenHeight + 66 + 26 + 10);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        obj.put("class", "OscilloscopeModule");

        obj.put("uuid", uuid.toString());
        obj.put("x-position", position.getX());
        obj.put("y-position", position.getY());

        return obj;
    }

    public static OscilloscopeModule fromJSON(JSONObject obj) {
        OscilloscopeModule noise = new OscilloscopeModule();

        noise.setUUID((String) obj.get("uuid"));
        noise.setPosition((double) obj.get("x-position"), (double) obj.get("y-position"));

        return noise;
    }
}
