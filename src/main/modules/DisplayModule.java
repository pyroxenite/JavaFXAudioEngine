package main.modules;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.TextAlignment;
import main.Module;
import utilities.ColorTheme;

public class DisplayModule extends Module {
    private float value = 0;

    public DisplayModule() {
        super("Display");
        addInput("Input");

        addOutput("Output").setSignalProvider(frameLength -> {
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
        translate(gc, position.getX(), position.getY());

        drawPaneAndTitleBar(gc);
        drawScreen(gc);
        drawPortsOnly(gc);

        translate(gc, -position.getX(), -position.getY());

        drawCables(gc);
    }

    private void drawScreen(GraphicsContext gc) {
        gc.setFill(ColorTheme.MODULE_FILL_1);
        gc.fillRoundRect(22, 26 + 5, width - 44, height - 26 - 10, 5, 5);

        gc.setFill(ColorTheme.TEXT_NORMAL);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.format("%.2f", value), width/2, 47);
    }
}
