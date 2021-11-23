package audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;

import main.interfaces.FrameGenerator;

public class SoundFileReader implements FrameGenerator {
    private byte[] bytes;

    private int bytesPerSample;
    private int channelCount;
    private boolean bigEndian;
    private int sampleCount;

    private int playheadPosition = 0;

    private File sourceFile = null;

    public SoundFileReader(File file) throws Exception {
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        AudioFormat format = ais.getFormat();
        bytesPerSample = format.getSampleSizeInBits() / 8;
        channelCount = format.getChannels();
        bigEndian = format.isBigEndian();
        sampleCount = ais.available() / channelCount / bytesPerSample;

        bytes = new byte[ais.available()];
        ais.read(bytes);

        sourceFile = file;
    }

    public SoundFileReader(String path) throws Exception {
        this(new File(path));
    }

    public float[] requestFrame(int n) {
        float[] frames = new float[n];
        float[] scaleFactors = new float[bytesPerSample];
        for (int j = 0; j < bytesPerSample; j++) {
            scaleFactors[j] = (bigEndian ? 1f / (1 << (j * 8)) : 1f / (1 << ((bytesPerSample - j - 1) * 8))) / 128;
        }

        for (int i = 0; i < n && playheadPosition < sampleCount; i++) {
            for (int j = 0; j < bytesPerSample; j++)
                frames[i] += scaleFactors[j] * bytes[playheadPosition * bytesPerSample * channelCount + j];
            playheadPosition += 1;
        }
        return frames;
    }

    public float getFrame(int i) {
        float frame = 0;
        for (int j = 0; j < bytesPerSample; j++) {
            float scaleFactor = (bigEndian ? 1f / (1 << (j * 8)) : 1f / (1 << ((bytesPerSample - j - 1) * 8))) / 128;
            frame += scaleFactor * bytes[i * bytesPerSample * channelCount + j];
        }
        return frame;
    }

    public float[] requestPreview(int pointCount, double startPercent, double endPercent) {
        if (endPercent == 1) endPercent = 0.9999;
        float[] preview = new float[pointCount];
        if (startPercent >= endPercent) return preview;
        for (int i=0; i<pointCount-1; i++) {
            double t1 = startPercent + (endPercent - startPercent)*i/(pointCount-1);
            double t2 = startPercent + (endPercent - startPercent)*(i + 1)/(pointCount-1);
            float min = 0, max = 0;
            for (int j=0; j<10; j++) {
                double t = t1 + (t2 - t1) * j / 10;
                float frame = getFrame((int) (t* sampleCount));
                min = Math.min(min, frame);
                max = Math.max(max, frame);
            }
            preview[i] = (Math.abs(max) > Math.abs(min))?max:min;
        }
        return preview;
    }

    public void setPlayhead(int position) {
        playheadPosition = position;
    }

    public void resetPlayhead() {
        playheadPosition = 0;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public static void main(String[] args) throws Exception {
        SoundFileReader sfr = new SoundFileReader("./src/clips/Kick 808 Thud.wav");

        float[] frames = sfr.requestPreview(200, 0, 1);

        for (int i = 0; i < frames.length; i++) {
            System.out.print(frames[i] + " ");
        }
    }

    public int getPlayhead() {
        return playheadPosition;
    }

    public File getSourceFile() {
        return sourceFile;
    }
}
