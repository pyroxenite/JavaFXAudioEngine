package main.modules;

import main.Module;
import main.components.Slider;

import java.util.ArrayList;

public class SequencerModule extends Module {
    private int numberOfSteps;

    public SequencerModule(int numberOfSteps) {
        super(numberOfSteps + "-step Sequencer");
        this.numberOfSteps = numberOfSteps;
        //add
    }

    @Override
    protected void updateGeometry() {
        width = 300;
        height = 26 + 100;
    }


}
