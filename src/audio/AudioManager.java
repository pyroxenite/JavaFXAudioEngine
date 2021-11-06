package audio;

import main.components.InputPort;
import main.components.Port;
import utility.FormatConverter;

import javax.sound.sampled.SourceDataLine;

public class AudioManager implements Runnable {
    private boolean threadIsRunning = false;
    private InputPort sourcePort = null;
    private int frameSize = 128;

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
                if (sourcePort != null && sourcePort.getCable() != null && outputLine.getBufferSize() - outputLine.available() < frameSize*10) {
                    float[] floats = sourcePort.requestFrame(frameSize);
                    outputLine.write(FormatConverter.toByteArray(floats, bytesPerSample), 0, frameSize);

                    //printBytes(sourcePort.requestFrame(frameSize));
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