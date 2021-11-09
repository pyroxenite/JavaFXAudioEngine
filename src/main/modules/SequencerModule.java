package main.modules;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import main.Module;
import main.components.*;
import utilities.ColorTheme;
import utilities.NoteFormater;
import utilities.Point;

import java.util.Arrays;

public class SequencerModule extends Module {
    private int numberOfSteps;
    private double[] notes;
    private boolean valueDragStarted = false;
    private int valueDragIndex = 0;
    private double t = 0;
    private int tempo = 260;
    private double dutyCycle = 0.5;

    public SequencerModule(int numberOfSteps) {
        super(numberOfSteps + "-step Sequencer");
        this.numberOfSteps = numberOfSteps;
        if (numberOfSteps == 8)
            notes = new double[]{ 36, 36+4, 36+7, 36+11, 36+12, 36+11, 36+7, 36+4 }; // stranger things
        else {
            notes = new double[numberOfSteps];
            Arrays.fill(notes, 36);
        }

        addOutput("Pitch").setSignalProvider(frameLength -> {
            float[] frame = new float[frameLength];
            double dt = tempo/60.0/44100;
            for (int i=0; i<frameLength; i++) {
                frame[i] = (float) (Math.floor(notes[(int) t])/64f - 1);
                t = (t+dt >= numberOfSteps)?0:t+dt;
            }
            return frame;
        }).setPosition(width - 11, height - 38);

        addOutput("Trigger").setSignalProvider(frameLength -> {
            float[] frame = new float[frameLength];
            for (int i=0; i<frameLength; i++) {
                if (t % 1.0 < dutyCycle/2.0) {
                    frame[i] = 1f;
                } else if (t % 1.0 < dutyCycle) {
                    frame[i] = 0.5f;
                } else {
                    frame[i] = 0f;
                }
            }
            return frame;
        }).setPosition(width - 11, height - 16);

    }

    @Override
    protected void updateGeometry() {
        if (numberOfSteps <= 8)
            width = 285;
        else
            width = 5 + 35*numberOfSteps;
        height = 26 + 100;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.transform(new Affine(1, 0, this.position.getX(), 0, 1, this.position.getY()));

        gc.setFill(ColorTheme.MODULE_BACKGROUND);
        if (isSelected) {
            gc.setLineWidth(2);
            gc.setStroke(ColorTheme.MODULE_BORDER_SELECTED);
        } else {
            gc.setLineWidth(1);
            gc.setStroke(ColorTheme.MODULE_BORDER);
        }
        double borderRadius = 30;
        gc.fillRoundRect(0, 0, width, height, borderRadius, borderRadius);
        gc.strokeRoundRect(0, 0, width, height, borderRadius, borderRadius);
        gc.setFill(ColorTheme.TEXT_NORMAL);
        gc.setTextAlign(TextAlignment.CENTER);
        String title = name;
        gc.fillText(name, width / 2, 18, width);

        gc.setStroke(ColorTheme.MODULE_BORDER);
        gc.setLineWidth(1);
        if (isSelected) {
            gc.strokeLine(1, 26, width - 1, 26);
        } else {
            gc.strokeLine(0, 26, width, 26);
        }

        gc.setTextAlign(TextAlignment.RIGHT);
        for (OutputPort outputPort: outputs) {
            outputPort.draw(gc);
            gc.setFill(ColorTheme.TEXT_NORMAL);
            gc.fillText(outputPort.getName(), outputPort.getPosition().getX()-10, outputPort.getPosition().getY()+4);
        }

        //getInput(0).draw(gc);

        gc.setTextAlign(TextAlignment.CENTER);
        double w = (width - 5.0)/numberOfSteps;
        for (int i=0; i<numberOfSteps; i++) {
            if (i == (int) t && t % 1.0 < dutyCycle)
                gc.setFill(ColorTheme.MODULE_FILL_2);
            else
                gc.setFill(ColorTheme.MODULE_FILL_1);
            gc.fillRoundRect(5 + w*i, 26 + 5, w - 5, 43, 5, 5);
            gc.setFill(ColorTheme.TEXT_NORMAL);
            gc.fillText(NoteFormater.numberToText((int) notes[i]), 5 + w*i + (w - 5)/2, 26 + 30);
        }

        gc.transform(new Affine(1, 0, -this.position.getX(), 0, 1, -this.position.getY()));

        if (temporaryCableReference != null)
            temporaryCableReference.draw(gc);

        //Cable cable = getInput(0).getCable();
        //if (cable != null)
        //    cable.draw(gc);
    }

    @Override
    public void handleMouseClicked(Point mousePosition) {
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
                valueDragIndex = (int) (relativePosition.getX() * numberOfSteps / width);
                valueDragIndex = Math.max(0, Math.min(numberOfSteps-1, valueDragIndex));
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
            notes[valueDragIndex] -= mouseDelta.getY()/20;
            notes[valueDragIndex] = Math.max(0, Math.min(127, notes[valueDragIndex]));
        }
    }
}
