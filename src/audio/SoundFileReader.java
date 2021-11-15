package audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.net.URL;

public class ReadFile {


    public static void main(String[] args) throws Exception {
        AudioInputStream ais = AudioSystem.getAudioInputStream(new File("./src/clips/Kick 808 Thud.wav"));

        AudioFormat format = ais.getFormat();
        System.out.println(format);
        int bytesPerSample = format.getSampleSizeInBits()/8;
        int channelCount = format.getChannels();

        byte[] bytes = new byte[ais.available()];
        ais.read(bytes);
        //bytes = ais.readAllBytes();
        for (int i=0; i<bytes.length; i+=1) {
            System.out.print(bytes[i] + " ");
        }
        //System.out.println(bytes.length);
    }
}
