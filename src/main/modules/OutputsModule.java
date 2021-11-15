package main.modules;

import audio.AudioIO;

import audio.AudioManager;
import main.Module;

/**
 * This module contains the final port of the audio pipeline. It is necessary to use this module to play sound.
 *
 * The module starts a new thread that is in charge of requesting audio frames from the input. Without this callback
 * none of the other modules will work.
 */
public class OutputsModule extends Module {
    AudioManager audioManager = new AudioManager(44100, 2);

    public OutputsModule() {
        super("System Outputs");

        this.addInput("Default Audio Device");

        new Thread(audioManager).start();
        audioManager.setSourcePort(getInput(0));
    }

    public void stop() {
        audioManager.terminateAudioThread();
    }
}