package main.modules;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import main.Module;
import main.components.Cable;
import main.components.InputPort;
import main.components.Knob;
import main.components.OutputPort;
import utilities.ColorTheme;
import utilities.MathFunctions;
import utilities.Point;

/*
TODO:
 * Display values
 */

public class ADSRModule extends Module {
    private boolean valueDragStarted = false;
    private int valueDragIndex;

    private int displayHeight = 70;

    private Knob attackKnob = new Knob("Attack", 30, 1, 2000, 100, "ms");
    private Knob decayKnob = new Knob("Decay", 30, 1, 2000, 200, "ms");
    private Knob sustainKnob = new Knob("Sustain", 30, 0, 100, 30, "%");
    private Knob releaseKnob = new Knob("Release", 30, 1, 10000, 500, "ms");
    private Knob minKnob = new Knob("Min", 30, -1, 1, 0);
    private Knob maxKnob = new Knob("Max", 30, -1, 1, 1);

    private double t = 0;
    private boolean triggered = false;

    private float softOutputSample = 0;

    public ADSRModule() {
        super("ADSR Envelope");
        double attack = attackKnob.getValue()/1000;
        double decay = decayKnob.getValue()/1000;

        addInput("Trigger").setPosition(11, 26 + displayHeight + 21);
        addOutput("Amplitude").setSignalProvider(frameLength -> {
            float[] frame = new float[frameLength];
            float[] trig = getInput(0).requestFrame(frameLength);
            for (int i=0; i<frameLength; i++) {
                if (trig[i] == 1 && !triggered) {
                    triggered = true;
                    t = 0;
                } else if (trig[i] != 1) {
                    triggered = false;
                }
                softOutputSample = (float) MathFunctions.lerp(softOutputSample, getAmplitude(t), 0.001/attack);
                frame[i] = softOutputSample;
                if (trig[i] == 0 && t < attack + decay)
                    t = attack + decay;
                if (trig[i] != 0 && t < attack + decay || trig[i] == 0)
                    t += 1/44100.0;
            }
            return frame;
        }).setPosition(width - 11, 26 + displayHeight + 21);

        double w = width/6;
        double x = w/2;
        double y = 153;
        attackKnob.setPosition(x, y);
        x += w;
        decayKnob.setPosition(x, y);
        x += w;
        sustainKnob.setPosition(x, y);
        x += w;
        releaseKnob.setPosition(x, y);
        x += w;
        minKnob.setPosition(x, y);
        x += w;
        maxKnob.setPosition(x, y);
    }

    @Override
    protected void updateGeometry() {
        width = 330;
        height = 26 + 150 + 18;
    }

    private double expDecay(double x) {
        return Math.exp(-x*4) * (1-x) * (1-x);
    }

    private float getAmplitude(double t) {
        double attack = attackKnob.getValue()/1000;
        double decay = decayKnob.getValue()/1000;
        double sustain = sustainKnob.getValue()/100;
        double release = releaseKnob.getValue()/1000;
        double a;
        if (t < attack)
            a = (float) (t/attack);
        else if (t < attack + decay) {
            a = (float) (expDecay((t - attack)/decay) * (1 - sustain) + sustain);
        } else {
            a = (float) (expDecay((t - attack - decay)/release) * sustain);
        }
        return (float) (a * (maxKnob.getValue() - minKnob.getValue()) + minKnob.getValue());
    }

    @Override
    public void draw(GraphicsContext gc) {
        translate(gc, position.getX(), position.getY());

        drawPaneAndTitleBar(gc);
        drawScreen(gc);
        drawPortsAndLabels(gc);

        attackKnob.draw(gc, isSelected);
        decayKnob.draw(gc, isSelected);
        sustainKnob.draw(gc, isSelected);
        releaseKnob.draw(gc, isSelected);
        minKnob.draw(gc, isSelected);
        maxKnob.draw(gc, isSelected);

        translate(gc, -position.getX(), -position.getY());

        drawCables(gc);
    }

    private void drawScreen(GraphicsContext gc) {
        double attack = attackKnob.getValue()/1000;
        double decay = decayKnob.getValue()/1000;
        double sustain = sustainKnob.getValue()/100;
        double release = releaseKnob.getValue()/1000;
        if (sustain == 0)
            release = 0;

        gc.setFill(ColorTheme.MODULE_FILL_1);
        gc.fillRoundRect(5, 26 + 5, width - 10, displayHeight, 5, 5);

        double totalTime = attack + decay + release;
        double w = width - 20;
        gc.setStroke(ColorTheme.MODULE_FILL_2);
        gc.setLineWidth(3);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.strokeLine(10, 26 + 5 + displayHeight - 5, 10 + attack/totalTime*w, 26 + 10);
        int N = 12;
        gc.beginPath();
        gc.moveTo(10 + attack/totalTime*w, 26 + 10);
        for (int i=1; i<=N; i++) {
            double t = Math.pow((double) i/N, 2);
            gc.lineTo(
                    10 + attack/totalTime*w + decay/totalTime*w*t,
                    26 + 10 + (displayHeight-10)*(1-sustain)*(1-expDecay(t))
            );
        }
        gc.stroke();
        gc.beginPath();
        gc.moveTo(10 + attack/totalTime*w + decay/totalTime*w, 26 + 10 + (displayHeight-10)*(1-sustain));
        for (int i=1; i<=N; i++) {
            double t = Math.pow((double) i/N, 2);
            gc.lineTo(
                    10 + (attack+decay)/totalTime*w + release/totalTime*w*t,
                    26 + 10 + (displayHeight-10)*(1-sustain) + (displayHeight-10)*sustain*(1-expDecay(t))
            );
        }
        gc.stroke();

        if (t <= totalTime) {
            gc.setStroke(ColorTheme.MODULE_BORDER_SELECTED);
            gc.setLineWidth(1);
            double x = 10 + w * t / totalTime;
            gc.strokeLine(x, 26 + 5, x, 26 + 5 + displayHeight);
        }
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
            } else if (relativePosition.getY() > 130) {
                valueDragStarted = true;
                valueDragIndex = (int) (relativePosition.getX() * 6 / width);
                valueDragIndex = Math.max(0, Math.min(5, valueDragIndex));
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
            switch (valueDragIndex) {
                case 0:
                    attackKnob.addValue((mouseDelta.getX()-mouseDelta.getY())/300.0);
                    break;
                case 1:
                    decayKnob.addValue((mouseDelta.getX()-mouseDelta.getY())/300.0);
                    break;
                case 2:
                    sustainKnob.addValue((mouseDelta.getX()-mouseDelta.getY())/300.0);
                    break;
                case 3:
                    releaseKnob.addValue((mouseDelta.getX()-mouseDelta.getY())/300.0);
                    break;
                case 4:
                    minKnob.addValue((mouseDelta.getX()-mouseDelta.getY())/300.0);
                    break;
                case 5:
                    maxKnob.addValue((mouseDelta.getX()-mouseDelta.getY())/300.0);
            }
        }
    }
}
