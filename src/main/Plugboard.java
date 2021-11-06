package main;

import audio.AudioIO;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

import main.modules.*;
import utility.ColorTheme;
import utility.Point;

import java.util.ArrayList;

public class Plugboard extends Canvas {
    ArrayList<Module> modules = new ArrayList<>();
    Module moduleUnderMouse = null;

    public Plugboard(double width, double height) {
        super(width, height);
        startMouseInput();

        AudioIO audioIO = new AudioIO();

        Module inputsModule = new InputsModule(audioIO);
        modules.add(inputsModule);
        inputsModule.setPosition(50,  50);

        Module oscillatorModule = new SineSquareOscillator();
        modules.add(oscillatorModule);
        oscillatorModule.setPosition(300,  200);

        Module oscillatorModule2 = new LowFrequencyOscillator();
        modules.add(oscillatorModule2);
        oscillatorModule2.setPosition(300,  300);

        OutputsModule outputsModule = new OutputsModule(audioIO);
        modules.add(outputsModule);
        outputsModule.setPosition(700,  50);

        Module knobModule = new KnobModule();
        modules.add(knobModule);
        knobModule.setPosition(100,  300);

        Module knobModule2 = new KnobModule();
        modules.add(knobModule2);
        knobModule2.setPosition(100,  420);

        Module knobModule3 = new KnobModule();
        modules.add(knobModule3);
        knobModule3.setPosition(100,  540);

        Module knobModule4 = new KnobModule();
        modules.add(knobModule4);
        knobModule4.setPosition(100,  660);

        /*Module mixerModule = new MixerModule(5);
        modules.add(mixerModule);
        mixerModule.setPosition(450,  300);*/

        /*Module sequencer = new SequencerModule(8);
        modules.add(sequencer);
        sequencer.setPosition(600,  300);*/

        Module display = new DisplayModule();
        modules.add(display);
        display.setPosition(500, 300);

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
