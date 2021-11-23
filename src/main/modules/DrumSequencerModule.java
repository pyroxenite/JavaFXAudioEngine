package main.modules;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import main.Module;
import main.components.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilities.ColorTheme;
import utilities.Point;

import javafx.scene.input.MouseEvent;

import java.util.Arrays;

/**
 * A drum sequencer can produce multiple synchronised trigger signals. They can then be used to trigger an ADSR or
 * sound clips.
 */
public class DrumSequencerModule extends Module {
    private int stepCount;
    private int channelCount;
    private boolean[][] sequence;
    private boolean valueDragStarted = false;
    private int valueDragIndex = 0;
    private double t = 0;
    private int buttonHeight = 20;

    private float[] commonTrigger = new float[2048];
    private boolean triggered = false;

    public DrumSequencerModule(int stepCount, int channelCount) {
        super(stepCount + "-beat Drum Sequencer");
        this.stepCount = stepCount;
        this.channelCount = channelCount;

        sequence = new boolean[channelCount][stepCount];

        addInput("Trigger").setPosition(11, 26 + (height-26)/2);

        for (int j=0; j<channelCount; j++) {
            final int finalJ = j;
            addOutput("Trigger").setFrameGenerator(frameLength -> {
                if (finalJ == 0)
                    commonTrigger = getInput(0).requestFrame(frameLength);
                float[] frame = new float[frameLength];
                for (int i = 0; i < frameLength; i++) {
                    if (commonTrigger[i] == 1 && !triggered) {
                        triggered = true;
                        if (finalJ == 0) {
                            t += 1;
                            t = t % stepCount;
                        }
                    } else if (commonTrigger[i] != 1 && triggered) {
                        triggered = false;
                    }
                    if (sequence[finalJ][(int) t])
                        frame[i] = commonTrigger[i];
                    else {
                        frame[i] = 0f;
                    }
                }
                return frame;
            }).setPosition(width - 11, 26 + 5 + buttonHeight / 2 + (buttonHeight + 5) * j);
        }
    }

    @Override
    protected void updateGeometry() {
        if (stepCount <= 8)
            width = 285;
        else
            width = 5 + 35*stepCount;
        width += 22;
        height = 26 + 5 + (buttonHeight + 5) * channelCount;
    }

    @Override
    public void draw(GraphicsContext gc) {
        translate(gc, position.getX(), position.getY());

        drawPaneAndTitleBar(gc);
        drawPortsOnly(gc);

        gc.setTextAlign(TextAlignment.CENTER);
        double w = (double) (width - 22 - 22 + 5)/stepCount;
        gc.setLineWidth(1);
        for (int j = 0; j< channelCount; j++) {
            for (int i = 0; i < stepCount; i++) {
                if (sequence[j][i]) {
                    gc.setFill(ColorTheme.MODULE_FILL_2);
                } else {
                    gc.setFill(ColorTheme.MODULE_FILL_1);
                }
                double x = 22 + w * i;
                double y = 26 + 5 + (buttonHeight + 5)*j;
                gc.fillRoundRect(x, y, w - 5, buttonHeight, 5, 5);
                gc.setStroke(ColorTheme.MODULE_BORDER_SELECTED);
                if (i == (int) t) {
                    gc.strokeRoundRect(x, y, w - 5, buttonHeight, 5, 5);
                }
                gc.setFill(ColorTheme.TEXT_NORMAL);
            }
        }

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
                if (relativePosition.getY() < 26 + 5 + (buttonHeight + 5)* channelCount) {
                    int triggerIndex = (int) ((relativePosition.getY() - 26) / (buttonHeight + 5));
                    triggerIndex = Math.max(0, Math.min(channelCount - 1, triggerIndex));
                    int stepIndex = (int) (relativePosition.getX() * stepCount / (width-22));
                    stepIndex = Math.max(0, Math.min(stepCount - 1, stepIndex));
                    sequence[triggerIndex][stepIndex] = !sequence[triggerIndex][stepIndex];
                } else {

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
                //notes[valueDragIndex] -= mouseDelta.getY() / 20;
                //notes[valueDragIndex] = Math.max(0, Math.min(127, notes[valueDragIndex]));
            } else {

            }
        }
    }

    @Override
    public void handleMouseReleased(Point mousePosition, Module moduleUnderMouse) {
        dragStarted = false;
        valueDragStarted = false;

        temporaryCableReference = null;

        if (moduleUnderMouse != null && moduleUnderMouse != this) {
            Point relativePosition = mousePosition.copy().subtract(moduleUnderMouse.getPosition());
            Port externalPortUnderMouse = moduleUnderMouse.findPortUnderMouse(relativePosition);

            if (externalPortUnderMouse != null && portUnderMouse.getClass() != externalPortUnderMouse.getClass()) {
                portUnderMouse.connectTo(externalPortUnderMouse);
            }
        }
    }

    public DrumSequencerModule setSequence(boolean[][] sequence) {
        this.sequence = sequence;
        return this;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        obj.put("class", "DrumSequencerModule");

        obj.put("uuid", uuid.toString());
        obj.put("x-position", position.getX());
        obj.put("y-position", position.getY());

        obj.put("step-count", stepCount);
        obj.put("channel-count", channelCount);

        JSONArray channels = new JSONArray();
        for (int i=0; i<channelCount; i++) {
            JSONArray channel = new JSONArray();
            for (int j=0; j<stepCount; j++) {
                channel.add(sequence[i][j]);
            }
            channels.add(channel);
        }
        obj.put("sequence", channels);

        System.out.println(obj);

        return obj;
    }

    public static DrumSequencerModule fromJSON(JSONObject obj) {
        int stepCount = (int) (long) obj.get("step-count");
        int channelCount = (int) (long) obj.get("channel-count");
        DrumSequencerModule drumSeq = new DrumSequencerModule(stepCount, channelCount);

        drumSeq.setUUID((String) obj.get("uuid"));
        drumSeq.setPosition((double) obj.get("x-position"), (double) obj.get("y-position"));

        boolean[][] sequence = new boolean[channelCount][stepCount];
        for (int i=0; i<channelCount; i++) {
            JSONArray channel = (JSONArray) ((JSONArray) obj.get("sequence")).get(i);
            for (int j=0; j<stepCount; j++) {
                sequence[i][j] = (boolean) channel.get(j);
            }
        }
        drumSeq.setSequence(sequence);

        return drumSeq;
    }
}
