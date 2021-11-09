package main.modules;

import main.Module;

public class SineSquareOscillator extends Module {
    private double ft = 0;

    public SineSquareOscillator() {
        super("Sine-Square Oscillator");
        addInput("Pitch");
        addInput("Amplitude");
        addInput("Shape");

        addOutput("Output").setSignalProvider((frameLength) -> {
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