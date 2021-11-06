// TEST


package audio;

import javax.sound.sampled.*;
import java.util.Arrays;

public class SinSynth {
    protected static final int SAMPLE_RATE = 44100;

    public static byte[] createSinWaveBuffer(double freq, int ms) {
        int samples = (int) (ms * SAMPLE_RATE / 1000);
        byte[] output = new byte[samples];

        double period = (double) SAMPLE_RATE / freq;
        for (int i = 0; i < output.length; i++) {
            double angle = 2.0 * Math.PI * i / period;
            output[i] = (byte) (Math.sin(angle) * 127f);
        }

        return output;
    }

    public static Mixer.Info getMixerInfo(String mixerName) {
        return Arrays.stream(AudioSystem.getMixerInfo())
                .filter(e -> e.getName().equalsIgnoreCase(mixerName)).findFirst().get();
    }

    public static void main(String[] args) throws LineUnavailableException {
        Mixer.Info airPodsDePharoah = getMixerInfo("MacBook Pro Speakers");
        //Mixer.Info macBookProSpeakers = getMixerInfo("MacBook Pro Speakers");
        final AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
        SourceDataLine line = AudioSystem.getSourceDataLine(af, airPodsDePharoah);
        //SourceDataLine line2 = AudioSystem.getSourceDataLine(af, macBookProSpeakers);
        line.open(af, SAMPLE_RATE);
        //line2.open(af, SAMPLE_RATE);
        line.start();
        //line2.start();

        System.out.println("Hey");

        for (int i=0; i<4; i++) {
            System.out.println(i);
            byte[] toneBuffer = createSinWaveBuffer(440, 1000);
            line.write(toneBuffer, 0, toneBuffer.length);
        }

        System.out.println("Hello");

        line.drain();
        line.close();
        //line2.drain();
        //line2.close();
    }

}