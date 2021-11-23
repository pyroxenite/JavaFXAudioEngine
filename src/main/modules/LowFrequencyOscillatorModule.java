package main.modules;

import main.Module;
import org.json.simple.JSONObject;

/**
 * This is an LFO module, used to modulate other parameters more or less slowly.
 */
public class LowFrequencyOscillatorModule extends Module {
    private double ft = 0;

    public LowFrequencyOscillatorModule() {
        super("Low Frequency Oscillator");
        addInput("Frequency");
        addInput("Amplitude");
        addInput("Offset");

        addOutput("Output").setFrameGenerator(frameLength -> {
            float[] frame = new float[frameLength];
            float[] freq = getInput(0).requestFrame(frameLength);
            float[] amp = getInput(1).requestFrame(frameLength);
            float[] offset = getInput(2).requestFrame(frameLength);
            for (int i = 0; i < frameLength; i++) {
                double f = 10 * Math.pow(2, (freq[i] * 64 / 12f));
                double num = Math.sin(2 * Math.PI * ft);
                frame[i] = (float) (num * (amp[i]+1)/2 + offset[i]);
                ft += f / 44100.0;
            }
            return frame;
        });
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        obj.put("class", "LowFrequencyOscillatorModule");

        obj.put("uuid", uuid.toString());
        obj.put("x-position", position.getX());
        obj.put("y-position", position.getY());

        return obj;
    }

    public static LowFrequencyOscillatorModule fromJSON(JSONObject obj) {
        LowFrequencyOscillatorModule lfo = new LowFrequencyOscillatorModule();

        lfo.setUUID((String) obj.get("uuid"));
        lfo.setPosition((double) obj.get("x-position"), (double) obj.get("y-position"));

        return lfo;
    }
}