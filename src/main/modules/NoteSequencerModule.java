package main.modules;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import main.Module;
import main.components.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilities.ColorTheme;
import utilities.MathFunctions;
import utilities.NoteFormater;
import utilities.Point;

import javafx.scene.input.MouseEvent;
import java.util.Arrays;

/**
 * This module can produce a pitch signal that encodes a user-customizable note sequence (a melody, a bass-line...).
 * It is meant to be connected to an oscillator and optionally an ADSR envelope.
 */
public class NoteSequencerModule extends Module {
    private int stepCount;
    private double[] notes;
    private boolean valueDragStarted = false;
    private int valueDragIndex = 0;
    private double t = 0;
    private Knob glideKnob = new Knob("Glide", 30, 0, 100, 0, "%");
    private Knob transposeKnob = new Knob("Transpose", 30, -36, 36, 0.5, "");
    private float currentPitchValue = 36;
    private boolean triggered = false;
    private float[] triggerIn;
    private boolean noteOn = false;

    public NoteSequencerModule(int stepCount) {
        super(stepCount + "-note Sequencer");
        this.stepCount = stepCount;
        if (stepCount == 8)
            notes = new double[]{ 36, 36+4, 36+7, 36+11, 36+12, 36+11, 36+7, 36+4 }; // stranger things
        else {
            notes = new double[stepCount];
            Arrays.fill(notes, 36);
        }

        glideKnob.setPosition(width/2 - 35, 105);
        transposeKnob.setPosition(width/2 + 35, 105).drawValueCentered().allowIntegersOnly();

        addInput("Trigger").setPosition(11, height  - 27 -  10);

        addOutput("Pitch").setFrameGenerator(frameLength -> {
            float[] frame = new float[frameLength];
            int transpose = (int) transposeKnob.getValue();
            triggerIn = getInput(0).requestFrame(frameLength);
            noteOn = triggerIn[0] == 0;
            for (int i=0; i<frameLength; i++) {
                currentPitchValue = (float) Math.max(-1, Math.min(1, MathFunctions.lerp(
                        currentPitchValue,
                        (float) (Math.floor(notes[(int) t] + transpose)/64f - 1),
                        0.000000001 + Math.pow(10, -2 - glideKnob.getValue()/30)
                )));
                frame[i] = currentPitchValue;
                if (triggerIn[i] == 1 && !triggered) {
                    triggered = true;
                    t += 1;
                    t = t % stepCount;
                } else if (triggerIn[i] != 1 && triggered) {
                    triggered = false;
                }
            }
            return frame;
        }).setPosition(width - 11, height - 38 - 10);

        addOutput("Trigger").setFrameGenerator(frameLength -> {
            if (triggerIn != null) {
                return triggerIn;
            } else {
                return new float[frameLength];
            }
        }).setPosition(width - 11, height - 16 - 10);

    }

    @Override
    protected void updateGeometry() {
        if (stepCount <= 8)
            width = 285;
        else
            width = 5 + 35* stepCount;
        height = 26 + 120;
    }

    @Override
    public void draw(GraphicsContext gc) {
        translate(gc, position.getX(), position.getY());

        drawPaneAndTitleBar(gc);
        drawPortsAndLabels(gc);

        gc.setTextAlign(TextAlignment.CENTER);
        double w = (width - 5.0)/ stepCount;
        for (int i = 0; i< stepCount; i++) {
            if (i == (int) t && noteOn)
                gc.setFill(ColorTheme.MODULE_FILL_2);
            else
                gc.setFill(ColorTheme.MODULE_FILL_1);
            gc.fillRoundRect(5 + w*i, 26 + 5, w - 5, 43, 5, 5);
            gc.setFill(ColorTheme.TEXT_NORMAL);
            gc.fillText(NoteFormater.numberToText((int) notes[i]), 5 + w*i + (w - 5)/2, 26 + 30);
        }

        glideKnob.setSelected(isSelected).draw(gc);
        transposeKnob.setSelected(isSelected).draw(gc);

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
                    valueDragIndex = (int) (relativePosition.getX() * stepCount / width);
                    valueDragIndex = Math.max(0, Math.min(stepCount - 1, valueDragIndex));
                } else {
                    valueDragStarted = true;
                    if (relativePosition.getX() < width/2) {
                        valueDragIndex = -1;
                        glideKnob.displayValue();
                    } else {
                        valueDragIndex = -2;
                        transposeKnob.displayValue();
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
                    glideKnob.addValue((mouseDelta.getX()-mouseDelta.getY())/300.0);
                } else if (valueDragIndex == -2) {
                    transposeKnob.addValue((mouseDelta.getX()-mouseDelta.getY())/300.0);
                }
            }
        }
    }

    @Override
    public void handleMouseReleased(Point mousePosition, Module moduleUnderMouse) {
        dragStarted = false;
        valueDragStarted = false;

        glideKnob.displayName();
        transposeKnob.displayName();

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

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        obj.put("class", "NoteSequencerModule");

        obj.put("uuid", uuid.toString());
        obj.put("x-position", position.getX());
        obj.put("y-position", position.getY());

        obj.put("step-count", stepCount);

        JSONArray channel = new JSONArray();
        for (int i=0; i<stepCount; i++) {
            channel.add(notes[i]);
        }
        obj.put("sequence", channel);

        obj.put("glide", glideKnob.getInternalValue());
        obj.put("transpose", transposeKnob.getInternalValue());

        return obj;
    }

    public static NoteSequencerModule fromJSON(JSONObject obj) {
        System.out.println(obj.get("step-count"));
        int stepCount = (int) (long) obj.get("step-count");
        NoteSequencerModule seq = new NoteSequencerModule(stepCount);

        seq.setUUID((String) obj.get("uuid"));
        seq.setPosition((double) obj.get("x-position"), (double) obj.get("y-position"));

        System.out.println(obj.get("sequence"));

        double[] sequence = new double[stepCount];
        for (int i=0; i<stepCount; i++) {
            sequence[i] = (double) ((JSONArray) obj.get("sequence")).get(i);
        }
        seq.setSequence(sequence);

        seq.glideKnob.setValue((double) obj.get("glide"));
        seq.transposeKnob.setValue((double) obj.get("transpose"));

        return seq;
    }
}
