package main.modules;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import main.Module;
import main.components.Cable;
import main.components.InputPort;
import main.components.OutputPort;
import main.components.SliderGauge;
import utilities.ColorTheme;
import utilities.MathFunctions;
import utilities.Point;

import java.util.ArrayList;

/**
 * This module implements a user-customizable weighted sum and displays the dB levels of its inputs.
 */
public class MixerModule extends Module {
    private ArrayList<SliderGauge> sliderGauges = new ArrayList<>();
    private boolean valueDragStarted = false;
    private int valueDragIndex = 0;

    public MixerModule(int numberOfInputs) {
        super("Mixer");

        for (int i=0; i<numberOfInputs; i++) {
            addInput("Input "+(i+1));
            sliderGauges.add(new SliderGauge(width - 44, true));
        }

        addOutput("Output").setFrameGenerator(frameLength -> {
            float[] sum = new float[frameLength];
            ArrayList<float[]> inputBytes = new ArrayList<>();
            for (int j = 0; j< sliderGauges.size(); j++) {
                double g = Math.pow(10, (sliderGauges.get(j).getSliderValue()-0.8)*3);
                float[] inputFrame = inputs.get(j).requestFrame(frameLength);
                inputBytes.add(inputFrame);
                sliderGauges.get(j).setGaugeValue(g * (1+MathFunctions.amplitudeInDecibels(inputFrame)/40));

                for (int i=0; i<frameLength; i++) {
                    sum[i] += inputBytes.get(j)[i] * g;
                }

            }
            return sum;
        });
    }

    @Override
    protected void updateGeometry() {
        width = 200;
        if (sliderGauges == null || outputs.size() < 1) return;
        height = 26 + 5 + (5 + SliderGauge.height)* sliderGauges.size();
        outputs.get(0).setPosition(width - 11, 26 + (height-26)/2);
        for (int i = 0; i  < sliderGauges.size(); i++) {
            getInput(i).setPosition(11, 26 + 5 + SliderGauge.height/2 + (SliderGauge.height + 5)*i);
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.transform(new Affine(1, 0, this.position.getX(),0, 1, this.position.getY()));

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
        gc.fillText(name, width/2, 18, width);

        gc.setStroke(ColorTheme.MODULE_BORDER);
        gc.setLineWidth(1);
        if (isSelected) {
            gc.strokeLine(1, 26, width-1, 26);
        } else {
            gc.strokeLine(0, 26, width, 26);
        }

        for (InputPort inputPort: inputs) {
            inputPort.draw(gc);
        }

        getOutput(0).draw(gc);

        gc.transform(new Affine(1, 0, 22,0, 1, 26 + 5));

        for (SliderGauge sliderGauge : sliderGauges) {
            sliderGauge.draw(gc);
            gc.transform(new Affine(1, 0, 0,0, 1, sliderGauge.getHeight() + 5));
        }

        gc.transform(new Affine(1, 0, -22,0, 1, -26-5-(SliderGauge.height + 5)* sliderGauges.size()));

        gc.transform(new Affine(1, 0, -this.position.getX(),0, 1, -this.position.getY()));

        if (temporaryCableReference != null)
            temporaryCableReference.draw(gc);

        for (InputPort inputPort: inputs) {
            Cable cable = inputPort.getCable();
            if (cable != null)
                cable.draw(gc);
        }
    }

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
                valueDragIndex = (int) (relativePosition.getY() - 26) / (5 + SliderGauge.height);
                valueDragIndex = Math.max(0, Math.min(sliderGauges.size()-1, valueDragIndex));
            }
        }
    }

    public void handleDrag(Point mousePosition, Point mouseDelta) {
        if (dragStarted) {
            setPosition(
                    position.getX() + mouseDelta.getX(),
                    position.getY() + mouseDelta.getY()
            );
        } else if (temporaryCableReference != null && portUnderMouse != null) {
            temporaryCableReference.setLooseEndPosition(mousePosition);
        } else if (valueDragStarted) {
            SliderGauge sliderGauge = sliderGauges.get(valueDragIndex);
            double delta = mouseDelta.getX()/ sliderGauge.getWidth();
            sliderGauge.setSliderValue(sliderGauge.getSliderValue() + delta);
        }
    }
}
