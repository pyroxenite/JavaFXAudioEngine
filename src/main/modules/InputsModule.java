package main.modules;

import java.util.List;
import javax.sound.sampled.Mixer;

import audio.AudioIO;

import main.Module;

public class InputsModule extends Module {
    public InputsModule(AudioIO audioIO) {
        super("System Inputs");
        List<Mixer.Info> infoList = audioIO.getAudioMixers();
        infoList.forEach(info -> this.addOutput(info.getName()));
    }
}
