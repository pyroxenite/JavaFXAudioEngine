package main.modules;

import main.Module;
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
}
