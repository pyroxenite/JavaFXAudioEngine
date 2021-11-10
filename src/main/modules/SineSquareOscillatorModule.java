package main.modules;

import main.Module;

public class SineSquareOscillatorModule extends Module {
    private double ft = 0;

    public SineSquareOscillatorModule() {
        super("Sine-Square Oscillator");
        addInput("Pitch");
        addInput("Amplitude");
        addInput("Shape");

        addOutput("Output").setSignalProvider((n) -> {
            float[] frame = new float[n];
            float[] pitch = getInput(0).requestFrames(n);
            float[] amp = getInput(1).requestFrames(n);
            float[] shape = getInput(2).requestFrames(n);
            for (int i = 0; i < n; i++) {
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