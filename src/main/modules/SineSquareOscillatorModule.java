package main.modules;

import main.Module;

/**
 * This module implements an oscillator with variable frequency, amplitude and shape. When the shape input receives a
 * signal equal to -1.0, the oscillator outputs a pure sine wave. When the shape input is equal to 1.0 it outputs a
 * square wave. Intermediate values produce a more or less saturated sine signal.
 */
public class SineSquareOscillatorModule extends Module {
    private double ft = 0;

    public SineSquareOscillatorModule() {
        super("Sine-Square Oscillator");
        addInput("Pitch");
        addInput("Amplitude");
        addInput("Shape");

        addOutput("Output").setFrameGenerator(frameLength -> {
            float[] frame = new float[frameLength];
            float[] pitch = getInput(0).requestFrame(frameLength);
            float[] amp = getInput(1).requestFrame(frameLength);
            float[] shape = getInput(2).requestFrame(frameLength);
            for (int i = 0; i < frameLength; i++) {
                double f = 440 * Math.pow(2, ((pitch[i] + 1) * 64 - 69) / 12f);
                double a = amp[i];//Math.pow(10, (amp[i] - 1) * 10 / 20f);
                double s = (1 + shape[i]) / 2;
                double num = Math.atan(Math.sin(2 * Math.PI * ft) * Math.exp(8 * s) / 10.0);
                double den = Math.atan(Math.exp(8 * s) / 10.0);
                frame[i] = (float) (num / den * a);
                ft += f / 44100.0;
            }
            return frame;
        });
    }
}