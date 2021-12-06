package audio;

import main.components.InputPort;
import utilities.FormatConverter;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutionException;

public class AudioManager implements Runnable {
    private boolean threadIsRunning = false;
    private InputPort sourcePort = null;
    private int bufferSize = 128;
    private int bytesPerSample;

    private int sampleRate;

    SourceDataLine outputLine;

    public AudioManager(int sampleRate, int bytesPerSample) {
        this.sampleRate = sampleRate;
        this.bytesPerSample = bytesPerSample;
    }

    public void setOutputLine(String mixerName) {
        try {
            outputLine = AudioIO.getOutputLine(mixerName, sampleRate, bytesPerSample);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {


        try {
            threadIsRunning = true;
            while (threadIsRunning) {
                //System.out.println(outputLine);
                if (outputLine == null) {
                    Thread.sleep(100);
                    continue;
                }
                if (sourcePort != null && sourcePort.getCable() != null && outputLine.getBufferSize() - outputLine.available() < bufferSize * 10) {
                    float[] floats = sourcePort.requestFrame(bufferSize);
                    outputLine.write(FormatConverter.toByteArray(floats, bytesPerSample), 0, bufferSize);
                } else if (sourcePort == null && outputLine.isOpen()) {
                    outputLine.drain();
                    outputLine.flush();
                    outputLine.close();
                }
                if (sourcePort != null && !outputLine.isOpen()) {
                    Thread.sleep(100);
                    outputLine.open();
                    outputLine.start();
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