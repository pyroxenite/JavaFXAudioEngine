package main.modules;

import main.Module;
import org.json.simple.JSONObject;
import utilities.MathFunctions;

import java.util.Random;

public class NoiseModule extends Module {
    private Random random = new Random();
    private float value = 0;
    //private float leakRate = 0.1f;

    public NoiseModule() {
        super("White/Pink Noise");

        addInput("Amplitude");
        addInput("Color");

        addOutput("Output").setFrameGenerator(frameLength -> {
            float[] frame = new float[frameLength];
            float[] amp = getInput(0).requestFrame(frameLength);
            float[] color = getInput(1).requestFrame(frameLength);
            for (int i=0; i<frameLength; i++) {
                float rand1 = (float) random.nextGaussian();
                float rand2 = (float) random.nextGaussian();
                float pinkness = (color[i] + 1)/2;
                value += -0.5 * value + rand1;
                frame[i] = amp[i] * MathFunctions.lerp(rand2, value, pinkness);
            }
            return frame;
        });
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        obj.put("class", "NoiseModule");

        obj.put("uuid", uuid.toString());
        obj.put("x-position", position.getX());
        obj.put("y-position", position.getY());

        return obj;
    }

    public static NoiseModule fromJSON(JSONObject obj) {
        NoiseModule noise = new NoiseModule();

        noise.setUUID((String) obj.get("uuid"));
        noise.setPosition((double) obj.get("x-position"), (double) obj.get("y-position"));

        return noise;
    }
}
