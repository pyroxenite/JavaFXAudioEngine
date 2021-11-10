package audio;

import main.components.InputPort;
import utilities.FormatConverter;

import javax.sound.sampled.SourceDataLine;

public class AudioManager implements Runnable {
    private boolean threadIsRunning = false;
    private InputPort sourcePort = null;
    private int bufferSize = 128;

    private int sampleRate;
    private int bytesPerSample;

    public AudioManager(int sampleRate, int bytesPerSample) {
        this.sampleRate = sampleRate;
        this.bytesPerSample = bytesPerSample;
    }

    @Override
    public void run() {
        AudioIO audioIO = new AudioIO();
        try {
            SourceDataLine outputLine = audioIO.getOutputLine("Default Audio Device", sampleRate, bytesPerSample);
            outputLine.open();
            outputLine.start();

            threadIsRunning = true;
            while (threadIsRunning) {
                if (sourcePort != null && sourcePort.getCable() != null && outputLine.getBufferSize() - outputLine.available() < bufferSize *10) {
                    float[] floats = sourcePort.requestFrames(bufferSize);
                    outputLine.write(FormatConverter.toByteArray(floats, bytesPerSample), 0, bufferSize);
                }
            }

            outputLine.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSourcePort(InputPort sourcePort) {
        this.sourcePort = sourcePort;
    }

    public void terminateAudioThread() {
        threadIsRunning = false;
    }
}