// TEST



package audio;

import javax.sound.sampled.*;

public class SampleAudio {

    private static long extendSign(long temp, int bitsPerSample) {
        int extensionBits = 64 - bitsPerSample;
        return (temp << extensionBits) >> extensionBits;
    }

    public static void main(String[] args) throws LineUnavailableException {
        float sampleRate = 8000;
        int sampleSizeBits = 16;
        int numChannels = 1; // Mono
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeBits, numChannels, true, true);
        TargetDataLine tdl = AudioSystem.getTargetDataLine(format);

        tdl.open(format);
        tdl.start();
        if (!tdl.isOpen()) {
            System.exit(1);
        }
        byte[] data = new byte[(int)sampleRate*3];
        int read = tdl.read(data, 0, (int)sampleRate*3);
        if (read > 0) {
            for (int i = 0; i < read-1; i = i + 2) {
                long val = ((data[i] & 0xffL) << 8L) | (data[i + 1] & 0xffL);
                long valf = extendSign(val, sampleSizeBits);
                System.out.println(i + ", " + valf);
            }
        }
        tdl.close();
    }
}