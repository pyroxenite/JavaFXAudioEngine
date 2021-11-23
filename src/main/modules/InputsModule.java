package main.modules;

import java.util.List;
import javax.sound.sampled.Mixer;

import audio.AudioIO;

import main.Module;
import org.json.simple.JSONObject;

/**
 * Not implemented.
 */
public class InputsModule extends Module {
    public InputsModule() {
        super("System Inputs");
        AudioIO audioIO = new AudioIO();
        List<Mixer.Info> infoList = audioIO.getAudioMixers();
        infoList.forEach(info -> this.addOutput(info.getName()));
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        obj.put("class", "InputsModule");

        obj.put("uuid", uuid.toString());
        obj.put("x-position", position.getX());
        obj.put("y-position", position.getY());

        return obj;
    }

    public static InputsModule fromJSON(JSONObject obj) {
        InputsModule inputsModule = new InputsModule();

        inputsModule.setUUID((String) obj.get("uuid"));
        inputsModule.setPosition((double) obj.get("x-position"), (double) obj.get("y-position"));

        return inputsModule;
    }
}
