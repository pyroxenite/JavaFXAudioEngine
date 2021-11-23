package main.modules;

import audio.AudioIO;

import audio.AudioManager;
import main.Module;
import org.json.simple.JSONObject;

/**
 * This module contains the final port of the audio pipeline. It is necessary to use this module to play sound.
 *
 * The module starts a new thread that is in charge of requesting audio frames from the input. Without this callback
 * none of the other modules will work.
 */
public class OutputsModule extends Module {
    AudioManager audioManager = new AudioManager(44100, 1);

    public OutputsModule() {
        super("System Outputs");

        this.addInput("Default Audio Device");

        new Thread(audioManager).start();
        audioManager.setSourcePort(getInput(0));
    }

    public void stop() {
        audioManager.terminateAudioThread();
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        obj.put("class", "OutputsModule");

        obj.put("uuid", uuid.toString());
        obj.put("x-position", position.getX());
        obj.put("y-position", position.getY());

        return obj;
    }

    public static OutputsModule fromJSON(JSONObject obj) {
        OutputsModule noise = new OutputsModule();

        noise.setUUID((String) obj.get("uuid"));
        noise.setPosition((double) obj.get("x-position"), (double) obj.get("y-position"));

        return noise;
    }

    @Override
    public void prepareForDelete() {
        audioManager.terminateAudioThread();
    }
}