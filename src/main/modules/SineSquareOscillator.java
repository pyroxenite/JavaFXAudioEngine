package main.modules;

import main.Module;

public class SineSquareOscillator extends Module {
    private double t = 0;

    public SineSquareOscillator() {
        super("Sine-Square Oscillator");
        addOutput("Output");
        addInput("Frequency");
        addInput("Amplitude");
        addInput("Shape");

        getOutput(0).setSignalProvider((frameLength) -> {
            float[] frame = new float[frameLength];
            float[] freq = getInput(0).requestFrame(frameLength);
            float[] amp = getInput(1).requestFrame(frameLength);
            float[] shape = getInput(2).requestFrame(frameLength);
            for (int i = 0; i < frameLength; i++) {
                double f = 440 * Math.pow(2, (freq[i] * 255 + 128 - 69) / 12f);
                double a = Math.pow(10, (amp[i] - 1) * 10 / 20f);
                float s = (shape[i] * 255 + 128) / 255f;
                double num = Math.atan(Math.sin(2 * Math.PI * f * t) * Math.exp(8 * s) / 10.0);
                double den = Math.atan(Math.exp(8 * s) / 10.0);
                frame[i] = (float) (num / den * a);
                t += 1 / 44100.0;
                if (t > 1/f)
                    t -= 1/f;
            }
            return frame;
        });
    }
}