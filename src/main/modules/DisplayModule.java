package main.modules;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import main.Module;
import main.components.Cable;
import utility.ColorTheme;
import utility.Point;

public class DisplayModule extends Module {
    private float value = 0;

    public DisplayModule() {
        super("Display");
        addInput("Input");
        addOutput("Output");

        getOutput(0).setSignalProvider(frameLength -> {
            float[] frame = getInput(0).requestFrame(frameLength);
            value = frame[0];
            return frame;
        });
    }

    @Override
    protected void updateGeometry() {
        width = 100;
        height = 60;
        if (outputs.size() == 1)
            outputs.get(0).setPosition(width - 11, 26 + (height-26)/2);
        if (inputs.size() == 1)
            inputs.get(0).setPosition(11, 26 + (height-26)/2);
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

        getOutput(0).draw(gc);
        getInput(0).draw(gc);

        gc.setFill(ColorTheme.MODULE_FILL_1);
        gc.fillRoundRect(22, 26 + 5, width - 44, height - 26 - 10, 5, 5);

        gc.setFill(ColorTheme.TEXT_NORMAL);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.format("%.2f", value), width/2, 47);



        gc.transform(new Affine(1, 0, -this.position.getX(),0, 1, -this.position.getY()));

        if (temporaryCableReference != null)
            temporaryCableReference.draw(gc);

        Cable cable = getInput(0).getCable();
        if (cable != null)
            cable.draw(gc);
    }
}
