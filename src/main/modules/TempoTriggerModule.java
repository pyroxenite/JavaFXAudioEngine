package main.modules;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import main.Module;
import main.components.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilities.Point;

import javafx.scene.input.MouseEvent;

/**
 * This module can produce a pitch signal that encodes a user-customizable note sequence (a melody, a bass-line...).
 * It is meant to be connected to an oscillator and optionally an ADSR envelope.
 */
public class TempoTriggerModule extends Module {
    private boolean valueDragStarted = false;
    private int valueDragIndex = 0;
    private double t = 0;
    private Knob dutyCycleKnob = new Knob("Duty Cycle", 30, 5, 100, 1, "%");
    private Knob tempoKnob = new Knob("Tempo", 30, 20, 6000, 0.2, "bpm");

    public TempoTriggerModule() {
        super("Tempo Trigger");

        dutyCycleKnob.setPosition(width/2.8 + 35, height - 40);
        tempoKnob.setPosition(width/2.8 - 35, height - 40).setMapMode(Knob.MapMode.EXPONENTIAL);

        addOutput("Trigger").setFrameGenerator(frameLength -> {
            float[] frame = new float[frameLength];
            double dt = tempoKnob.getValue()/60/44100;
            for (int i=0; i<frameLength; i++) {
                if (t < dutyCycleKnob.getValue()/100/2.0) {
                    frame[i] = 1f;
                } else if (t < dutyCycleKnob.getValue()/100.0) {
                    frame[i] = 0.5f;
                } else {
                    frame[i] = 0f;
                }
                t = (t+dt >= 1)?0:t+dt;
            }
            return frame;
        }).setPosition(width - 11, 26 + (height - 26)/2);
    }

    @Override
    protected void updateGeometry() {
        width = 240;
        height = 26 + 70;
    }

    @Override
    public void draw(GraphicsContext gc) {
        translate(gc, position.getX(), position.getY());

        drawPaneAndTitleBar(gc);
        drawPortsAndLabels(gc);

        gc.setTextAlign(TextAlignment.CENTER);

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
                valueDragStarted = true;
                if (relativePosition.getX() < width/2.8) {
                    valueDragIndex = 1;
                    tempoKnob.displayValue();
                } else {
                    valueDragIndex = 2;
                    dutyCycleKnob.displayValue();
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
            if (valueDragIndex == 2) {
                dutyCycleKnob.addValue((mouseDelta.getX()-mouseDelta.getY())/300.0);
            } else if (valueDragIndex == 1) {
                tempoKnob.addValue((mouseDelta.getX()-mouseDelta.getY())/300.0);
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

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        obj.put("class", "TempoTriggerModule");

        obj.put("uuid", uuid.toString());
        obj.put("x-position", position.getX());
        obj.put("y-position", position.getY());

        obj.put("tempo", tempoKnob.getInternalValue());
        obj.put("duty-cycle", dutyCycleKnob.getInternalValue());

        return obj;
    }

    public static TempoTriggerModule fromJSON(JSONObject obj) {
        TempoTriggerModule tempo = new TempoTriggerModule();

        tempo.setUUID((String) obj.get("uuid"));
        tempo.setPosition((double) obj.get("x-position"), (double) obj.get("y-position"));

        tempo.tempoKnob.setValue((double) obj.get("tempo"));
        tempo.dutyCycleKnob.setValue((double) obj.get("duty-cycle"));

        return tempo;
    }
}
