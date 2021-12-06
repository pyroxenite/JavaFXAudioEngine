package audio;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** A collection of static utilities related to the audio system. */
public class AudioIO {
    /**
     * Displays every audio mixer available on the current system.
     */
    public static void printAudioMixers() {
        System.out.println("Mixers:");
        Arrays.stream(AudioSystem.getMixerInfo())
                .forEach(e -> System.out.println("- name=\"" + e.getName()
                        + "\" description=\"" + e.getDescription() + " by " + e.getVendor() + "\""));
    }

    public static List<Mixer.Info> getAudioMixers() {
        List<Mixer.Info> infosList = new ArrayList<>(Arrays.asList(AudioSystem.getMixerInfo()));
        infosList.removeIf(info -> Pattern.matches("Port .*", info.getName()));
        infosList = infosList.stream().distinct().collect(Collectors.toList());
        return infosList;
    }

    public static Mixer.Info getMixerInfo(String mixerName) {
        return Arrays.stream(AudioSystem.getMixerInfo())
                .filter(e -> e.getName().equalsIgnoreCase(mixerName)).findFirst().get();
    }

    public static TargetDataLine getInputLine(String mixerName, int sampleRate, int bytesPerSample) throws LineUnavailableException {
        Mixer.Info mixerInfo = getMixerInfo(mixerName);
        AudioFormat format = new AudioFormat(sampleRate, 8*bytesPerSample, 1, true, false);
        return AudioSystem.getTargetDataLine(format, mixerInfo);
    }

    public static SourceDataLine getOutputLine(String mixerName, int sampleRate, int bytesPerSample) throws LineUnavailableException {
        Mixer.Info mixerInfo = getMixerInfo(mixerName);
        AudioFormat format = new AudioFormat(sampleRate, 8*bytesPerSample, 1, true, false);
        return AudioSystem.getSourceDataLine(format, mixerInfo);
    }

    public static void main(String[] args) {
        List<Mixer.Info> infosList = AudioIO.getAudioMixers();
        infosList.forEach(info -> System.out.println('"' + info.getName() + '"'));
    }
}