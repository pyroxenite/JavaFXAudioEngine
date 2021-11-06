package main.modules;

import audio.AudioIO;

import audio.AudioManager;
import main.Module;

public class OutputsModule extends Module {
    AudioManager audioManager = new AudioManager(44100, 1);

    public OutputsModule(AudioIO audioIO) {
        super("System Outputs");

        this.addInput("Default Audio Device");

        new Thread(audioManager).start();
        audioManager.setSourcePort(getInput(0));
    }

    public void stop() {
        audioManager.terminateAudioThread();
    }
}