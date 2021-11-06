package main.modules;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import main.Module;
import main.components.Cable;
import main.components.InputPort;
import main.components.OutputPort;
import main.components.Slider;
import utility.ColorTheme;
import utility.Point;

import java.util.ArrayList;

public class MixerModule extends Module {
    private ArrayList<Slider> sliders = new ArrayList<>();
    private boolean valueDragStarted = false;
    private int valueDragIndex = 0;

    public MixerModule(int numberOfInputs) {
        super("Mixer");

        for (int i=0; i<numberOfInputs; i++) {
            addInput("Input "+(i+1));
            sliders.add(new Slider(width - 44, true));
        }

        addOutput("Output");
    }

    @Override
    protected void updateGeometry() {
        width = 200;
        if (sliders == null || outputs.size() < 1) return;
        height = 26 + 5 + (5 + Slider.height)*sliders.size();
        outputs.get(0).setPosition(width - 11, 26 + (height-26)/2);
        for (int i = 0; i  < sliders.size(); i++) {
            getInput(i).setPosition(11, 26 + 5 + Slider.height/2 + (Slider.height + 5)*i);
        }

        outputs.get(0).setSignalProvider((frameLength -> {
            float[] sum = new float[frameLength];
            ArrayList<float[]> inputBytes = new ArrayList<>();
            for (int j=0; j<sliders.size(); j++)
                inputBytes.add(inputs.get(j).requestFrame(frameLength));
            for (int i=0; i<frameLength; i++) {
                for (int j=0; j<sliders.size(); j++) {
                    double g = Math.pow(10, (sliders.get(j).getValue()-0.8))*5;
                    sum[i] += inputBytes.get(j)[i] * g;
                }
            }
            return sum;
        }));
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

        for (Slider slider: sliders) {
            slider.draw(gc);
            gc.transform(new Affine(1, 0, 0,0, 1, slider.getHeight() + 5));
        }

        gc.transform(new Affine(1, 0, -22,0, 1, -26-5-(Slider.height + 5)*sliders.size()));

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
                valueDragIndex = (int) (relativePosition.getY() - 26) / (5 + Slider.height);
                valueDragIndex = Math.max(0, Math.min(sliders.size()-1, valueDragIndex));
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
            Slider slider = sliders.get(valueDragIndex);
            double delta = mouseDelta.getX()/slider.getWidth();
            slider.setValue(slider.getValue() + delta);
        }
    }
}
