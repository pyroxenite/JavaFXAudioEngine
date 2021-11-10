package main;

import audio.AudioIO;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

import main.modules.*;
import utilities.ColorTheme;
import utilities.Point;

import java.util.ArrayList;

public class Plugboard extends Canvas {
    ArrayList<Module> modules = new ArrayList<>();
    Module moduleUnderMouse = null;

    public Plugboard(double width, double height) {
        super(width, height);
        startMouseInput();

        AudioIO audioIO = new AudioIO();

        modules.add(new SineSquareOscillatorModule().setPosition(650,  200));
        modules.add(new LowFrequencyOscillatorModule().setPosition(300,  350));
        modules.add(new OutputsModule(audioIO).setPosition(700,  50));
        modules.add(new KnobModule().setPosition(100,  100));
        modules.add(new KnobModule().setPosition(100,  210));
        modules.add(new KnobModule().setPosition(100,  320));
        modules.add(new KnobModule().setPosition(100,  430));

        /*Module mixerModule = new MixerModule(5);
        modules.add(mixerModule);
        mixerModule.setPosition(450,  300);*/
        modules.add(new MixerModule(5).setPosition(500, 500));

        modules.add(new SequencerModule(8).setPosition(300,  100));
        modules.add(new ADSRModule().setPosition(300,  250));

        modules.add(new DisplayModule().setPosition(700, 400));

        //outputModule.getInput(0).connectTo(oscillatorModule.getOutput(0));

        redraw();
    }

    public void redraw() {
        GraphicsContext gc = this.getGraphicsContext2D();
        gc.setFill(ColorTheme.PLUGBOARD_BACKGROUND);
        gc.fillRect(0, 0, getWidth(), getHeight());
        for (Module module: modules) {
            module.draw(gc);
        }
    }

    private void startMouseInput(){
        // Add mouse event handlers for the source
        Point lastPosition = new Point(0, 0);

        this.setOnMousePressed((MouseEvent event) -> {
            event.setDragDetect(true);
            lastPosition.set((int) event.getX(), (int) event.getY());

            // deselect last selected module
            if (moduleUnderMouse != null)
                moduleUnderMouse.deselect();

            moduleUnderMouse = findModuleAt(event.getX(), event.getY());
            if (moduleUnderMouse != null) {
                moduleUnderMouse.select();
                bringModuleToFront(moduleUnderMouse);
            }
            if (moduleUnderMouse != null) {
                Point mousePosition = new Point((int) event.getX(), (int) event.getY());
                moduleUnderMouse.handleMouseClicked(mousePosition);
            }
            redraw();
        });

        this.setOnDragDetected((MouseEvent event) -> {
            //canvas.startFullDrag();
            //System.out.println("Drag detected");
        });

        this.setOnMouseDragged((MouseEvent event) -> {
            //System.out.println("Mouse dragged");
            Point delta = new Point(
                    (int) event.getX()-lastPosition.getX(),
                    (int) event.getY()-lastPosition.getY()
            );
            lastPosition.set((int) event.getX(), (int) event.getY());
            if (moduleUnderMouse != null) {
                moduleUnderMouse.handleDrag(lastPosition, delta);
                redraw();
            }
        });

        this.setOnMouseReleased((MouseEvent event) -> {
            if (moduleUnderMouse != null) {
                Point mousePosition = new Point((int) event.getX(), (int) event.getY());
                moduleUnderMouse.handleMouseReleased(mousePosition, findModuleAt(event.getX(), event.getY()));
            }
            redraw();
        });
    }

    private Module findModuleAt(double mouseX, double mouseY) {
        for (int i=modules.size()-1; i>=0; i--) {
            Module module = modules.get(i);
            Rectangle2D rect = module.getBoundingBox();
            if (rect.contains(mouseX, mouseY))
                return module;
        }
        return null;
    }

    private void bringModuleToFront(Module module) {
        modules.add(module);
        modules.remove(module);
    }

    public ArrayList<Module> getModules() {
        return modules;
    }
}
