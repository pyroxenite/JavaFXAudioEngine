package main;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.modules.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import utilities.ColorTheme;
import utilities.Point;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

/**
 * The plugboard manages and draws modules, and distributes user input based on the modules' bounding box.
 */
public class Plugboard extends Canvas {
    private ArrayList<Module> modules = new ArrayList<>();
    private Module selectedModule = null;
    private String clipBoard = null;
    private Point pastePosition = new Point();
    private Stage stage;

    public Plugboard(double width, double height, Stage stage) {
        super(width, height);
        startMouseInput();
        startKeyboardInput();
        createContextMenu(stage);
        this.stage = stage;

        //Demos.setUpDrumDemo(this, stage);
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
            if (selectedModule != null)
                selectedModule.deselect();

            selectedModule = findModuleAt(event.getX(), event.getY());
            if (selectedModule != null) {
                selectedModule.select();
                bringModuleToFront(selectedModule);
                selectedModule.handleMouseClicked(event);
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
            if (selectedModule != null) {
                selectedModule.handleDrag(lastPosition, delta);
                redraw();
            }
        });

        this.setOnMouseReleased((MouseEvent event) -> {
            if (selectedModule != null) {
                Point mousePosition = new Point((int) event.getX(), (int) event.getY());
                selectedModule.handleMouseReleased(mousePosition, findModuleAt(event.getX(), event.getY()));
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

    public void copySelectedModule() {
        if (selectedModule != null) {
            clipBoard = selectedModule.toJSON().toString();
            pastePosition = selectedModule.getPosition().copy();
            pastePosition.add(new Point(20, 20));
        }
    }

    public void pasteModule() {
        try {
            if (clipBoard != null) {
                //Module addedModule = Module.fromJSON(clipBoard);
                Module addedModule = addModuleFromJSON(clipBoard, true);
                if (addedModule == null)
                    return;
                if (selectedModule != null)
                    selectedModule.deselect();
                addedModule.select();
                selectedModule = addedModule;
                addedModule.setPosition(pastePosition.copy());
                pastePosition.add(new Point(20, 20));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Module addModuleFromJSON(String str, boolean newUIID) {
        Module module = null;
        JSONParser parser = new JSONParser();
        try {
            JSONObject obj = (JSONObject) parser.parse(str);
            String moduleClassName = (String) obj.getOrDefault("class", "Module");
            Class moduleClass = Class.forName("main.modules." + moduleClassName);
            if (moduleClassName.compareTo("PlayerModule") == 0) {
                Method fromJsonMethod = moduleClass.getMethod("fromJSON", JSONObject.class, Stage.class);
                module = (Module) fromJsonMethod.invoke(null, obj, stage);
            } else {
                Method fromJsonMethod = moduleClass.getMethod("fromJSON", JSONObject.class);
                module = (Module) fromJsonMethod.invoke(null, obj);
            }
            if (newUIID)
                module.newRandomUUID();
            modules.add(module);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return module;
    }

    public void deleteSelectedModule() {
        if (selectedModule != null) {
            selectedModule.prepareForDelete();
            modules.remove(selectedModule);
        }
    }

    public void duplicateSelectedModule() {
        copySelectedModule();
        pasteModule();
    }

    public void cutSelectedModule() {
        copySelectedModule();
        deleteSelectedModule();
    }

    public void saveToFile() {
        JSONObject plugboard = new JSONObject();
        JSONArray modulesJSON = new JSONArray();
        JSONObject cablesJSON = new JSONObject();
        for (Module m: modules) {
            modulesJSON.add(m.toJSON());
            cablesJSON.put(m.getUUID(), m.getInputConnectionsAsJSON());
        }

        plugboard.put("modules", modulesJSON);
        plugboard.put("cables", cablesJSON);

        System.out.println(plugboard);

        FileChooser fileChooser = new FileChooser();

        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Little Endian Plugboard Projet", "*.lepp");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                PrintWriter writer = new PrintWriter(file);
                writer.println(plugboard);
                writer.close();
            } catch (Exception e) {}
        }
    }

    public void loadFromFile(File file) {
        clearPlugboard();

        JSONParser jsonParser = new JSONParser();
        JSONObject obj;

        try (FileReader reader = new FileReader(file)) {
            obj = (JSONObject) jsonParser.parse(reader);
            for (Object o: (JSONArray) obj.get("modules")) {
                addModuleFromJSON(o.toString(), false);
            }
            JSONObject cables = (JSONObject) obj.get("cables");
            cables.keySet().forEach(targetModuleUUID -> {
                Module targetModule = getModule(UUID.fromString((String) targetModuleUUID));
                JSONObject connections = (JSONObject) cables.get(targetModuleUUID);
                connections.keySet().forEach(stringInputPortIndex -> {
                    int inputPortIndex = Integer.parseInt((String) stringInputPortIndex);
                    JSONObject connection = (JSONObject) connections.get(stringInputPortIndex);
                    Module sourceModule = getModule(UUID.fromString((String) connection.get("source-module-UUID")));
                    int outputPortIndex = (int) (long) connection.get("output-port-index");
                    sourceModule.getOutput(outputPortIndex).connectTo(targetModule.getInput(inputPortIndex));
                });


            });
        } catch (Exception e) {

        }
    }

    public Module getModule(UUID uuid) {
        for (Module m: modules)
            if (m.getUUID().compareTo(uuid) == 0)
                return m;
        return null;
    }

    public void clearPlugboard() {
        /*Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("");
        alert.setHeaderText("Confirm delete");
        alert.setContentText("Are you sure you want to clear the plugboard? This action is irreversible.");
        alert.showAndWait().ifPresent(rs -> {
            if (rs == ButtonType.OK) {
                System.out.println("Pressed OK.");
            }
        });*/
        for (Module module: modules) {
            module.prepareForDelete();
        }
        modules = new ArrayList<>();
    }

    public void requestAndOpenFile() {
        File userFile = new FileChooser().showOpenDialog(stage);
        loadFromFile(userFile);
    }

    public void createContextMenu(Stage stage) {
        ContextMenu contextMenu = new ContextMenu();

        final Menu generators = new Menu("Generators");
        MenuItem sineSquare = new MenuItem("Sine/Square Oscillator");
        sineSquare.setOnAction(e -> {
            SineSquareOscillatorModule osc = new SineSquareOscillatorModule();
            modules.add(osc.setPosition(
                    contextMenu.getX() - contextMenu.getWidth()/2,
                    contextMenu.getY() - contextMenu.getHeight()/2
            ));
        });
        MenuItem noise = new MenuItem("Noise Generator");
        noise.setOnAction(e -> {
            NoiseModule noiseModule = new NoiseModule();
            modules.add(noiseModule.setPosition(
                    contextMenu.getX() - contextMenu.getWidth()/2,
                    contextMenu.getY() - contextMenu.getHeight()/2
            ));
        });
        MenuItem player = new MenuItem("Clip Player");
        player.setOnAction(e -> {
            PlayerModule playerModule = new PlayerModule(stage);
            modules.add(playerModule.setPosition(
                    contextMenu.getX() - contextMenu.getWidth()/2,
                    contextMenu.getY() - contextMenu.getHeight()/2
            ));
        });

        generators.getItems().addAll(sineSquare, noise, player);

        final Menu envelopes = new Menu("Envelopes");
        MenuItem adsr = new MenuItem("ADSR Envelope");
        adsr.setOnAction(e -> {
            ADSRModule adsrModule = new ADSRModule();
            modules.add(adsrModule.setPosition(
                    contextMenu.getX() - contextMenu.getWidth()/2,
                    contextMenu.getY() - contextMenu.getHeight()/2
            ));
        });
        MenuItem lfo = new MenuItem("Low Frequency Oscillator");
        lfo.setOnAction(e -> {
            LowFrequencyOscillatorModule lfoModule = new LowFrequencyOscillatorModule();
            modules.add(lfoModule.setPosition(
                    contextMenu.getX() - contextMenu.getWidth()/2,
                    contextMenu.getY() - contextMenu.getHeight()/2
            ));
        });
        MenuItem riseFall = new MenuItem("Rise/Fall Envelope");
        riseFall.setDisable(true);
        envelopes.getItems().addAll(adsr, lfo, riseFall);

        final Menu triggers = new Menu("Triggers");
        MenuItem tempoTrig = new MenuItem("Tempo Trigger");
        tempoTrig.setOnAction(e -> {
            TempoTriggerModule seq = new TempoTriggerModule();
            modules.add(seq.setPosition(
                    contextMenu.getX() - contextMenu.getWidth()/2,
                    contextMenu.getY() - contextMenu.getHeight()/2
            ));
        });
        MenuItem noteSeq = new MenuItem("Note Sequencer");
        noteSeq.setOnAction(e -> {
            NoteSequencerModule seq = new NoteSequencerModule(8);
            modules.add(seq.setPosition(
                    contextMenu.getX() - contextMenu.getWidth()/2,
                    contextMenu.getY() - contextMenu.getHeight()/2
            ));
        });
        MenuItem drumSeq = new MenuItem("Drum Sequencer");
        drumSeq.setOnAction(e -> {
            DrumSequencerModule seq = new DrumSequencerModule(8, 4);
            modules.add(seq.setPosition(
                    contextMenu.getX() - contextMenu.getWidth()/2,
                    contextMenu.getY() - contextMenu.getHeight()/2
            ));
        });
        triggers.getItems().addAll(tempoTrig, noteSeq, drumSeq);

        final Menu effects = new Menu("Effects");
        effects.setDisable(true);
        //MenuItem noteSeq = new MenuItem("Note Sequencer");
        //noteSeq.setAccelerator(new KeyCodeCombination(KeyCode.N));
        //MenuItem drumSeq = new MenuItem("Drum Sequencer");
        //drumSeq.setAccelerator(new KeyCodeCombination(KeyCode.D));
        effects.getItems().addAll();

        final Menu io = new Menu("I/O");
        MenuItem inputs = new MenuItem("System Inputs");
        inputs.setOnAction(e -> {
            InputsModule inputsModule = new InputsModule();
            modules.add(inputsModule.setPosition(
                    contextMenu.getX() - contextMenu.getWidth()/2,
                    contextMenu.getY() - contextMenu.getHeight()/2
            ));
        });
        MenuItem outputs = new MenuItem("System Outputs");
        outputs.setOnAction(e -> {
            OutputsModule outputsModule = new OutputsModule();
            modules.add(outputsModule.setPosition(
                    contextMenu.getX() - contextMenu.getWidth()/2,
                    contextMenu.getY() - contextMenu.getHeight()/2
            ));
        });
        io.getItems().addAll(inputs, outputs);

        final Menu displays = new Menu("Displays");
        MenuItem valueDisp = new MenuItem("Display Value");
        valueDisp.setOnAction(e -> {
            DisplayModule displayModule = new DisplayModule();
            modules.add(displayModule.setPosition(
                    contextMenu.getX() - contextMenu.getWidth()/2,
                    contextMenu.getY() - contextMenu.getHeight()/2
            ));
        });
        MenuItem grapher = new MenuItem("Grapher Module");
        grapher.setOnAction(e -> {
            GrapherModule grapherModule = new GrapherModule();
            modules.add(grapherModule.setPosition(
                    contextMenu.getX() - contextMenu.getWidth()/2,
                    contextMenu.getY() - contextMenu.getHeight()/2
            ));
        });
        MenuItem oscilloscope = new MenuItem("Oscilloscope");
        oscilloscope.setOnAction(e -> {
            OscilloscopeModule oscilloscopeModule = new OscilloscopeModule();
            modules.add(oscilloscopeModule.setPosition(
                    contextMenu.getX() - contextMenu.getWidth()/2,
                    contextMenu.getY() - contextMenu.getHeight()/2
            ));
        });
        MenuItem spectrum = new MenuItem("Spectrum Viewer");
        spectrum.setDisable(true);
        spectrum.setOnAction(e -> {

        });
        displays.getItems().addAll(valueDisp, grapher, oscilloscope, spectrum);

        final Menu utilities = new Menu("Utilities");
        MenuItem mixer = new MenuItem("Mixer");
        mixer.setOnAction(e -> {
            MixerModule mixerModule = new MixerModule(5);
            modules.add(mixerModule.setPosition(
                    contextMenu.getX() - contextMenu.getWidth()/2,
                    contextMenu.getY() - contextMenu.getHeight()/2
            ));
        });
        MenuItem knob = new MenuItem("Knob");
        knob.setOnAction(e -> {
            KnobModule knobModule = new KnobModule();
            modules.add(knobModule.setPosition(
                    contextMenu.getX() - contextMenu.getWidth()/2,
                    contextMenu.getY() - contextMenu.getHeight()/2
            ));
        });
        utilities.getItems().addAll(mixer, knob);

        contextMenu.getItems().addAll(
                generators,
                envelopes,
                triggers,
                effects,
                displays,
                io,
                utilities
        );

        setOnContextMenuRequested(e -> contextMenu.show(getScene().getWindow(), e.getScreenX(), e.getScreenY()));
    }


}
