package main;

import audio.AudioIO;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import javafx.scene.transform.Affine;
import javafx.stage.Stage;
import main.modules.*;
import utilities.ColorTheme;
import utilities.Point;

import java.util.ArrayList;

/**
 * The plugboard manages and draws modules, and distributes user input based on the modules' bounding box.
 */
public class Plugboard extends Canvas {
    private ArrayList<Module> modules = new ArrayList<>();
    private Module moduleUnderMouse = null;

    public Plugboard(double width, double height, Stage stage) {
        super(width, height);
        startMouseInput();
        startKeyboardInput();

        Demos.setUpDrumDemo(this, stage);
        //Demos.setUpSoundGenDemo(this, stage);
        //Demos.setUpMainDemo(this, stage);


        //Demos.setUpTest1(this, stage);
        //Demos.setUpTest2(this, stage);

        redraw();
    }

    public void redraw() {
        GraphicsContext gc = this.getGraphicsContext2D();
        //gc.setTransform(new Affine(0.5, 0, 0, 0, 0.5, 0));

        gc.setFill(ColorTheme.PLUGBOARD_BACKGROUND);
        gc.fillRect(0, 0, getWidth(), getHeight());
        for (Module module: modules) {
            module.draw(gc);
        }
    }

    private void startMouseInput() {
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
                moduleUnderMouse.handleMouseClicked(event);
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

    private void startKeyboardInput() {
        this.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                System.out.println("Key Pressed: " + event.getCode());
            }
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
        modules.remove(module); // first occurrence only
    }

    public ArrayList<Module> getModules() {
        return modules;
    }
}
