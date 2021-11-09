package main.modules;

import main.Module;

public class LowFrequencyOscillator extends Module {
    private double ft = 0;

    public LowFrequencyOscillator() {
        super("Low Frequency Oscillator");
        addInput("Frequency");
        addInput("Amplitude");
        addInput("Offset");

        addOutput("Output").setSignalProvider(frameLength -> {
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
}