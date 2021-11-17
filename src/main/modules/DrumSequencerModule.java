package main.modules;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import main.Module;
import main.components.*;
import utilities.ColorTheme;
import utilities.Point;

import javafx.scene.input.MouseEvent;

/**
 * A drum sequencer can produce multiple synchronised trigger signals. They can then be used to trigger an ADSR or
 * sound clips.
 */
public class DrumSequencerModule extends Module {
    private int numberOfSteps;
    private int numberOfTriggers;
    private boolean[][] sequence;
    private boolean valueDragStarted = false;
    private int valueDragIndex = 0;
    private double t = 0;
    private Knob dutyCycleKnob = new Knob("Duty Cycle", 30, 5, 100, 1, "%");
    private Knob tempoKnob = new Knob("Tempo", 30, 20, 6000, 0.5, "bpm");
    private int buttonHeight = 20;

    public DrumSequencerModule(int numberOfSteps, int numberOfTriggers) {
        super(numberOfSteps + "-beat Drum Sequencer");
        this.numberOfSteps = numberOfSteps;
        this.numberOfTriggers = numberOfTriggers;

        sequence = new boolean[numberOfTriggers][numberOfSteps];

        for (int j=0; j<numberOfTriggers; j++) {
            final int finalJ = j;
            addOutput("Trigger").setFrameGenerator(frameLength -> {
                double dt = tempoKnob.getValue() / 60 / 44100;
                float[] frame = new float[frameLength];
                for (int i = 0; i < frameLength; i++) {
                    if (sequence[finalJ][(int) t] & t % 1.0 < dutyCycleKnob.getValue() / 100 / 2.0)
                        frame[i] = 1f;
                    else if (sequence[finalJ][(int) t] & t % 1.0 < dutyCycleKnob.getValue() / 100.0)
                        frame[i] = 0.5f;
                    else {
                        frame[i] = 0f;
                    }
                    if (finalJ == 0)
                        t = (t + dt >= numberOfSteps) ? 0 : t + dt;
                }
                return frame;
            }).setPosition(width - 11, 26 + 5 + buttonHeight / 2 + (buttonHeight + 5) * j);
        }

        dutyCycleKnob.setPosition(width/2 - 40, height - 40);
        tempoKnob.setPosition(width/2 + 40, height - 40).setMapMode(Knob.MapMode.EXPONENTIAL);

    }

    @Override
    protected void updateGeometry() {
        if (numberOfSteps <= 8)
            width = 285;
        else
            width = 5 + 35*numberOfSteps;
        width += 22;
        height = 26 + 5 + (buttonHeight + 5) * numberOfTriggers + 66;
    }

    @Override
    public void draw(GraphicsContext gc) {
        translate(gc, position.getX(), position.getY());

        drawPaneAndTitleBar(gc);
        drawPortsOnly(gc);

        gc.setTextAlign(TextAlignment.CENTER);
        double w = (double) (width - 5 - 22 + 5)/numberOfSteps;
        gc.setLineWidth(1);
        for (int j=0; j<numberOfTriggers; j++) {
            for (int i = 0; i < numberOfSteps; i++) {
                if (i == (int) t && t % 1.0 < dutyCycleKnob.getValue() / 100.0) {
                    gc.setFill(ColorTheme.MODULE_FILL_2);
                    gc.setStroke(ColorTheme.MODULE_FILL_2);
                } else {
                    gc.setFill(ColorTheme.MODULE_FILL_1);
                    gc.setStroke(ColorTheme.MODULE_BORDER);
                }
                if (sequence[j][i])
                    gc.fillRoundRect(5 + w * i, 26 + 5 + (buttonHeight + 5)*j, w - 5, buttonHeight, 5, 5);
                else
                    gc.strokeRoundRect(5 + w * i, 26 + 5 + (buttonHeight + 5)*j, w - 5, buttonHeight, 5, 5);
                gc.setFill(ColorTheme.TEXT_NORMAL);
            }
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
                if (relativePosition.getY() < 26 + 5 + (buttonHeight + 5)*numberOfTriggers) {
                    int triggerIndex = (int) ((relativePosition.getY() - 26) / (buttonHeight + 5));
                    triggerIndex = Math.max(0, Math.min(numberOfTriggers - 1, triggerIndex));
                    int stepIndex = (int) (relativePosition.getX() * numberOfSteps / (width-22));
                    stepIndex = Math.max(0, Math.min(numberOfSteps - 1, stepIndex));
                    sequence[triggerIndex][stepIndex] = !sequence[triggerIndex][stepIndex];
                } else {
                    valueDragStarted = true;
                    if (relativePosition.getX() < width/2) {
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
                //notes[valueDragIndex] -= mouseDelta.getY() / 20;
                //notes[valueDragIndex] = Math.max(0, Math.min(127, notes[valueDragIndex]));
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

    public DrumSequencerModule setSequence(boolean[][] sequence) {
        this.sequence = sequence;
        return this;
    }
}
