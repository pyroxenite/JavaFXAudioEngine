package main.modules;

import audio.SoundFileReader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Affine;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.Module;
import main.components.*;
import utilities.ColorTheme;
import utilities.Point;

import java.io.File;

/**
 * This module plays sounds stored in WAV format.
 */
public class PlayerModule extends Module {
    private SoundFileReader soundFileReader;
    private boolean triggered = false;
    private Stage stage;
    private double screenHeight = 100;
    private boolean valueDragStarted = false;
    private int valueDragIndex;
    private float[] preview = new float[2];

    private Knob startKnob = new Knob("Start", 30, 0, 100, 0, "%");
    private Knob endKnob = new Knob("End", 30, 0, 100, 1, "%");

    public PlayerModule(Stage stage) {
        super("Clip Player");
        this.stage = stage;

        addInput("Trigger").setPosition(11, height - 35);

        addOutput("Output").setFrameGenerator(frameLength -> {
            if (soundFileReader == null)
                return new float[frameLength];
            float[] frames = soundFileReader.requestFrame(frameLength);
            float[] trig = getInput(0).requestFrame(frameLength);
            for (int i=0; i<frameLength; i++) {
                if (trig[i] == 1 && !triggered) {
                    triggered = true;
                    soundFileReader.setPlayhead((int) (soundFileReader.getSampleCount() * startKnob.getValue()/100));
                } else if (trig[i] == 0) {
                    soundFileReader.setPlayhead(soundFileReader.getSampleCount());
                    triggered = false;
                } else if (trig[i] != 1) {
                    triggered = false;
                }
            }
            return frames;
        }).setPosition(width - 11, height - 35);

        startKnob.setPosition(width/2 - 35, height - 40);
        endKnob.setPosition(width/2 + 35, height - 40);
    }

    @Override
    protected void updateGeometry() {
        width = 300;
        height = (int) (screenHeight + 66 + 26 + 10);
    }

    @Override
    public void draw(GraphicsContext gc) {
        translate(gc, position.getX(), position.getY());

        drawPaneAndTitleBar(gc);
        drawPortsAndLabels(gc);

        gc.setFill(ColorTheme.MODULE_FILL_1);
        gc.fillRoundRect(5, 26 + 5, width - 10, screenHeight, 5, 5);

        gc.setLineJoin(StrokeLineJoin.ROUND);
        gc.setStroke(ColorTheme.MODULE_FILL_2);
        gc.setLineWidth(3);
        gc.beginPath();
        gc.moveTo(10, 26 + 5 + screenHeight/2 - (screenHeight-10)/2*preview[0]);
        for (int i=1; i<preview.length; i++) {
            double t = (double) i/(preview.length - 1);
            gc.lineTo(10 + t*(width - 20), 26 + 5 + screenHeight/2 - (screenHeight-10)/2*preview[i]);
        }
        gc.stroke();

        double percent = (double) soundFileReader.getPlayhead() / soundFileReader.getSampleCount();
        percent = (percent - startKnob.getValue()/100) / (endKnob.getValue()/100 - startKnob.getValue()/100);
        if (percent >= 0 && percent < 1) {
            gc.setStroke(ColorTheme.MODULE_BORDER_SELECTED);
            gc.setLineWidth(1);
            double x = 10 + (width - 20) * percent;
            gc.strokeLine(x, 26 + 5, x, 26 + 5 + screenHeight);
        }

        startKnob.setSelected(isSelected).draw(gc);
        endKnob.setSelected(isSelected).draw(gc);

        gc.transform(new Affine(1, 0, -this.position.getX(), 0, 1, -this.position.getY()));

        drawCables(gc);
    }

    public void openUserClip() {
        openFile(new FileChooser().showOpenDialog(stage));
    }

    public void openFile(File file) {
        try {
            if (file != null) {
                soundFileReader = new SoundFileReader(file);
                this.name = file.getName().replaceFirst("[.][^.]+$", "");;
                updatePreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (soundFileReader != null) {
            preview = soundFileReader.requestPreview(100, startKnob.getValue()/100, endKnob.getValue()/100);
        }
    }

    @Override
    public void handleMouseClicked(Point mousePosition) {
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
            } else if (relativePosition.getY() > 26 + screenHeight + 5) {
                valueDragStarted = true;
                valueDragIndex = (int) (relativePosition.getX() * 2 / width);
                valueDragIndex = Math.max(0, Math.min(5, valueDragIndex));
                if (valueDragIndex == 0)
                    startKnob.displayValue();
                else if (valueDragIndex == 1)
                    endKnob.displayValue();
            } else {
                openUserClip();
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
            if (valueDragIndex == 0)
                startKnob.addValue((mouseDelta.getX()-mouseDelta.getY())/1000.0);
            else if (valueDragIndex == 1)
                endKnob.addValue((mouseDelta.getX()-mouseDelta.getY())/1000.0);
            updatePreview();
        }
    }

    @Override
    public void handleMouseReleased(Point mousePosition, Module moduleUnderMouse) {
        dragStarted = false;
        valueDragStarted = false;
        startKnob.displayName();
        endKnob.displayName();

        temporaryCableReference = null;

        if (moduleUnderMouse != null && moduleUnderMouse != this) {
            Point relativePosition = mousePosition.copy().subtract(moduleUnderMouse.getPosition());
            Port externalPortUnderMouse = moduleUnderMouse.findPortUnderMouse(relativePosition);

            if (externalPortUnderMouse != null && portUnderMouse.getClass() != externalPortUnderMouse.getClass()) {
                portUnderMouse.connectTo(externalPortUnderMouse);
            }
        }
    }
}
