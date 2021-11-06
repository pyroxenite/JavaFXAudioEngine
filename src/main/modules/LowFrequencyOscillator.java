package main.modules;

import main.Module;

public class LowFrequencyOscillator extends Module {
    private double t = 0;

    public LowFrequencyOscillator() {
        super("LFO");
        addOutput("Output");
        addInput("Frequency");
        addInput("Amplitude");
        addInput("Offset");

        getOutput(0).setSignalProvider((frameLength) -> {
            float[] frame = new float[frameLength];
            float[] freq = getInput(0).requestFrame(frameLength);
            float[] amp = getInput(1).requestFrame(frameLength);
            float[] offset = getInput(2).requestFrame(frameLength);
            for (int i = 0; i < frameLength; i++) {
                double f = 4 * Math.pow(2, freq[i]*255/12.0);
                double sig = Math.sin(2 * Math.PI * f * t);
                frame[i] = (float) (sig * (1+amp[i])/2.0 + offset[i]);
                t += 1 / 44100.0;
            }
            return frame;
        });
    }
}