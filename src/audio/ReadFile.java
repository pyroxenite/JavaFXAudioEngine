package audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.net.URL;

public class ReadFile {


    public static void main(String[] args) throws Exception {
        AudioInputStream ais = AudioSystem.getAudioInputStream(new File("./src/clips/Kick 808 Thud.wav"));
        System.out.println(ais.getFormat());
        AudioFormat format = ais.getFormat();
        int bytesPerSample = format.getSampleSizeInBits()/8;
        int channelCount = format.getChannels();
        byte[] bytes = ais.readAllBytes();
        for (int i=0; i<bytes.length/6; i+=format.getFrameSize()) {
            System.out.println(bytes[i]);
        }
        //System.out.println(bytes.length);
    }
}
