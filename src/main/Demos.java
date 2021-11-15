package main;

import javafx.stage.Stage;
import main.modules.*;

import java.io.File;
import java.util.ArrayList;

public final class Demos {
    static void setUpDrumDemo(Plugboard pb, Stage stage) {
        ArrayList<Module> modules = pb.getModules();

        // Initialize modules
        DrumSequencerModule drumSequencer = new DrumSequencerModule(16, 3);
        drumSequencer.setSequence(new boolean[][] {
                {true,  true,  false, false, true,  false, false, false, true,  false, false, false, true,  false, false, false},
                {false, false, false, false, false, false, true,  false, false, true,  false, false, false, false, true,  false},
                {false, true,  false, true,  false, true,  false, true,  false, true,  false, true,  false, true,  false, true }
        }).setPosition(40, 361);
        modules.add(drumSequencer);

        PlayerModule kick = new PlayerModule(stage);
        kick.openFile(new File("./src/clips/Kick 808 Thud.wav"));
        modules.add(kick.setPosition(400 + 270, 50));

        PlayerModule snare = new PlayerModule(stage);
        snare.openFile(new File("./src/clips/Snare 70s.wav"));
        modules.add(snare.setPosition(400 + 270, 260));

        PlayerModule hihat = new PlayerModule(stage);
        hihat.openFile(new File("./src/clips/Hihat Closed.wav"));
        modules.add(hihat.setPosition(400+ 270, 470));

        Module mixer = new MixerModule(3).setPosition(745 + 270, 361);
        modules.add(mixer);

        Module outputsModule = new OutputsModule().setPosition(990 + 270, 385);
        modules.add(outputsModule);

        // Connect ports via cables
        drumSequencer.getOutput(0).connectTo(kick.getInput(0));
        drumSequencer.getOutput(1).connectTo(snare.getInput(0));
        drumSequencer.getOutput(2).connectTo(hihat.getInput(0));

        kick.getOutput(0).connectTo(mixer.getInput(0));
        snare.getOutput(0).connectTo(mixer.getInput(1));
        hihat.getOutput(0).connectTo(mixer.getInput(2));

        mixer.getOutput(0).connectTo(outputsModule.getInput(0));
    }

    public static void setUpTestDemo(Plugboard pb, Stage stage) {
        ArrayList<Module> modules = pb.getModules();

        modules.add(new NoiseModule());

        modules.add(new SineSquareOscillatorModule());
        modules.add(new KnobModule());
        modules.add(new KnobModule());
        modules.add(new KnobModule());
        modules.add(new KnobModule());

        modules.add(new GrapherModule());
        modules.add(new DisplayModule());

        modules.add(new ADSRModule());
        modules.add(new ADSRModule());
        modules.add(new NoteSequencerModule(5));

        Module mixer = new MixerModule(5).setPosition(745 + 270, 361);
        modules.add(mixer);

        Module outputsModule = new OutputsModule().setPosition(990 + 270, 385);
        modules.add(outputsModule);

        DrumSequencerModule drumSequencer = new DrumSequencerModule(16, 3);
        drumSequencer.setSequence(new boolean[][] {
                {true,  true,  false, false, true,  false, false, false, true,  false, false, false, true,  false, false, false},
                {false, false, false, false, false, false, true,  false, false, true,  false, false, false, false, true,  false},
                {false, true,  false, true,  false, true,  false, true,  false, true,  false, true,  false, true,  false, true }
        }).setPosition(40, 361);
        modules.add(drumSequencer);

        PlayerModule kick = new PlayerModule(stage);
        kick.openFile(new File("./src/clips/Kick 808 Thud.wav"));
        modules.add(kick.setPosition(400 + 270, 50));

        PlayerModule snare = new PlayerModule(stage);
        snare.openFile(new File("./src/clips/Snare 70s.wav"));
        modules.add(snare.setPosition(400 + 270, 260));

        PlayerModule hihat = new PlayerModule(stage);
        hihat.openFile(new File("./src/clips/Hihat Closed.wav"));
        modules.add(hihat.setPosition(400+ 270, 470));
    }
}
