package main.modules;

import main.Module;

public class LowFrequencyOscillatorModule extends Module {
    private double ft = 0;

    public LowFrequencyOscillatorModule() {
        super("Low Frequency Oscillator");
        addInput("Frequency");
        addInput("Amplitude");
        addInput("Offset");

        addOutput("Output").setSignalProvider(n -> {
            float[] frame = new float[n];
            float[] freq = getInput(0).requestFrames(n);
            float[] amp = getInput(1).requestFrames(n);
            float[] offset = getInput(2).requestFrames(n);
            for (int i = 0; i < n; i++) {
                double f = 10 * Math.pow(2, (freq[i] * 64 / 12f));
                double num = Math.sin(2 * Math.PI * ft);
                frame[i] = (float) (num * (amp[i]+1)/2 + offset[i]);
                ft += f / 44100.0;
            }
            return frame;
        });
    }
}