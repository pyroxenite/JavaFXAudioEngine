package main.modules;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import main.Module;
import main.components.*;
import utilities.ColorTheme;
import utilities.NoteFormater;
import utilities.Point;

import javafx.scene.input.MouseEvent;
import java.util.Arrays;

/**
 * This module can produce a pitch signal that encodes a user-customizable note sequence (a melody, a bass-line...).
 * It is meant to be connected to an oscillator and optionally an ADSR envelope.
 */
public class NoteSequencerModule extends Module {
    private int numberOfSteps;
    private double[] notes;
    private boolean valueDragStarted = false;
    private int valueDragIndex = 0;
    private double t = 0;
    private Knob dutyCycleKnob = new Knob("Duty Cycle", 30, 5, 100, 1, "%");
    private Knob tempoKnob = new Knob("Tempo", 30, 20, 6000, 0.2, "bpm");

    public NoteSequencerModule(int numberOfSteps) {
        super(numberOfSteps + "-note Sequencer");
        this.numberOfSteps = numberOfSteps;
        if (numberOfSteps == 8)
            notes = new double[]{ 36, 36+4, 36+7, 36+11, 36+12, 36+11, 36+7, 36+4 }; // stranger things
        else {
            notes = new double[numberOfSteps];
            Arrays.fill(notes, 36);
        }

        dutyCycleKnob.setPosition(width/2.5 - 35, 105);
        tempoKnob.setPosition(width/2.5 + 35, 105).setMapMode(Knob.MapMode.EXPONENTIAL);

        addOutput("Pitch").setFrameGenerator(frameLength -> {
            float[] frame = new float[frameLength];
            double dt = tempoKnob.getValue()/60/44100;
            for (int i=0; i<frameLength; i++) {
                frame[i] = (float) (Math.floor(notes[(int) t])/64f - 1);
                t = (t+dt >= numberOfSteps)?0:t+dt;
            }
            return frame;
        }).setPosition(width - 11, height - 38 - 10);

        addOutput("Trigger").setFrameGenerator(frameLength -> {
            float[] frame = new float[frameLength];
            for (int i=0; i<frameLength; i++) {
                if (t % 1.0 < dutyCycleKnob.getValue()/100/2.0) {
                    frame[i] = 1f;
                } else if (t % 1.0 < dutyCycleKnob.getValue()/100.0) {
                    frame[i] = 0.5f;
                } else {
                    frame[i] = 0f;
                }
            }
            return frame;
        }).setPosition(width - 11, height - 16 - 10);

    }

    @Override
    protected void updateGeometry() {
        if (numberOfSteps <= 8)
            width = 285;
        else
            width = 5 + 35*numberOfSteps;
        height = 26 + 120;
    }

    @Override
    public void draw(GraphicsContext gc) {
        translate(gc, position.getX(), position.getY());

        drawPaneAndTitleBar(gc);
        drawPortsAndLabels(gc);

        gc.setTextAlign(TextAlignment.CENTER);
        double w = (width - 5.0)/numberOfSteps;
        for (int i=0; i<numberOfSteps; i++) {
            if (i == (int) t && t % 1.0 < dutyCycleKnob.getValue()/100.0)
                gc.setFill(ColorTheme.MODULE_FILL_2);
            else
                gc.setFill(ColorTheme.MODULE_FILL_1);
            gc.fillRoundRect(5 + w*i, 26 + 5, w - 5, 43, 5, 5);
            gc.setFill(ColorTheme.TEXT_NORMAL);
            gc.fillText(NoteFormater.numberToText((int) notes[i]), 5 + w*i + (w - 5)/2, 26 + 30);
        }

        dutyCycleKnob.setSelected(isSelected).draw(gc);
        tempoKnob.setSelected(isSelected).draw(gc);

        gc.transform(new Affine(1, 0, -this.position.getX(), 0, 1, -this.position.getY()));

        drawCables(gc);
    }

    @Override
    public void handleMouseClicked(MouseEvent event) {
        Point mousePosition = new Point((int) event.getX(), (int) event.getY());
        valueDragStarted = false;
        Point relativePosition = mousePosition.copy().subtract(position);
        if (relativePosition.getY() < 26) {
            dragStarted = true;
        } else {
            portUnderMouse = findPortUnderMouse(relativePosition);
            if (portUnderMouse != null) {
                if (portUnderMouse.getClass() == OutputPort.class)
                    temporaryCableReference = new Cable((OutputPort) portUnderMouse, mousePosition);
                else
                    temporaryCableReference = new Cable((InputPort) portUnderMouse, mousePosition);
            } else {
                if (relativePosition.getY() < 26 + 5 + 43) {
                    valueDragStarted = true;
                    valueDragIndex = (int) (relativePosition.getX() * numberOfSteps / width);
                    valueDragIndex = Math.max(0, Math.min(numberOfSteps - 1, valueDragIndex));
                } else {
                    valueDragStarted = true;
                    if (relativePosition.getX() < width/2.5) {
                        valueDragIndex = -1;
                        dutyCycleKnob.displayValue();
                    } else {
                        valueDragIndex = -2;
                        tempoKnob.displayValue();
                    }
                }
            }
        }
    }

    @Override
    public void handleDrag(Point mousePosition, Point mouseDelta) {
        if (dragStarted) {
            setPosition(
                    position.getX() + mouseDelta.getX(),
                    position.getY() + mouseDelta.getY()
            );
        } else if (temporaryCableReference != null && portUnderMouse != null) {
            temporaryCableReference.setLooseEndPosition(mousePosition);
        } else if (valueDragStarted) {
            if (valueDragIndex >= 0) {
                notes[valueDragIndex] -= mouseDelta.getY() / 20;
                notes[valueDragIndex] = Math.max(0, Math.min(127, notes[valueDragIndex]));
            } else {
                if (valueDragIndex == -1) {
                    dutyCycleKnob.addValue((mouseDelta.getX()-mouseDelta.getY())/300.0);
                } else if (valueDragIndex == -2) {
                    tempoKnob.addValue((mouseDelta.getX()-mouseDelta.getY())/300.0);
                }
            }
        }
    }

    @Override
    public void handleMouseReleased(Point mousePosition, Module moduleUnderMouse) {
        dragStarted = false;
        valueDragStarted = false;

        dutyCycleKnob.displayName();
        tempoKnob.displayName();

        temporaryCableReference = null;

        if (moduleUnderMouse != null && moduleUnderMouse != this) {
            Point relativePosition = mousePosition.copy().subtract(moduleUnderMouse.getPosition());
            Port externalPortUnderMouse = moduleUnderMouse.findPortUnderMouse(relativePosition);

            if (externalPortUnderMouse != null && portUnderMouse.getClass() != externalPortUnderMouse.getClass()) {
                portUnderMouse.connectTo(externalPortUnderMouse);
            }
        }
    }

    public NoteSequencerModule setSequence(double[] notes) {
        this.notes = notes;
        return this;
    }
}
