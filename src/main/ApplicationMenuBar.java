package main;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class ApplicationMenuBar extends MenuBar {
    public ApplicationMenuBar(Plugboard pb) {
        super();

        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac"))
            this.useSystemMenuBarProperty().set(true);

        this.getMenus().addAll(
                createFileMenu(pb),
                createEditMenu(pb),
                createInsertMenu(pb),
                createHelpMenu()
        );

    }

    private static Menu createFileMenu(Plugboard pb) {
        final String os = System.getProperty("os.name");
        KeyCombination.Modifier ctrl;
        if (os != null && os.startsWith("Mac"))
            ctrl = KeyCodeCombination.META_DOWN;
        else
            ctrl = KeyCodeCombination.CONTROL_DOWN;

        MenuItem newItem = new MenuItem("New plugboard...");
        newItem.setAccelerator(new KeyCodeCombination(KeyCode.N, ctrl));
        newItem.setOnAction(e -> {
            pb.clearPlugboard();
        });

        MenuItem openItem = new MenuItem("Open...");
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, ctrl));
        openItem.setOnAction(e -> {
            pb.requestAndOpenFile();
        });

        MenuItem saveItem = new MenuItem("Save");
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, ctrl));
        saveItem.setOnAction(e -> {
            pb.saveToFile();
        });

        MenuItem saveAsItem = new MenuItem("Save as...");
        saveAsItem.setAccelerator(new KeyCodeCombination(KeyCode.S, ctrl, KeyCombination.SHIFT_DOWN));

        final Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(
                newItem,
                openItem,
                new SeparatorMenuItem(),
                saveItem,
                saveAsItem
        );

        return fileMenu;
    }

    private static Menu createEditMenu(Plugboard pb) {
        final String os = System.getProperty("os.name");
        KeyCombination.Modifier ctrl;
        if (os != null && os.startsWith("Mac"))
            ctrl = KeyCodeCombination.META_DOWN;
        else
            ctrl = KeyCodeCombination.CONTROL_DOWN;

        MenuItem undoItem = new MenuItem("Undo");
        undoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, ctrl));
        undoItem.setDisable(true);

        MenuItem redoItem = new MenuItem("Redo");
        redoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, ctrl, KeyCombination.SHIFT_DOWN));
        redoItem.setDisable(true);

        MenuItem cutItem = new MenuItem("Cut");
        cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X, ctrl));
        cutItem.setOnAction(e -> pb.cutSelectedModule());

        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, ctrl));
        copyItem.setOnAction(e -> pb.copySelectedModule());

        MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, ctrl));
        pasteItem.setOnAction(e -> pb.pasteModule());

        MenuItem duplicateItem = new MenuItem("Duplicate");
        duplicateItem.setAccelerator(new KeyCodeCombination(KeyCode.D, ctrl));
        duplicateItem.setOnAction(e -> pb.duplicateSelectedModule());

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setAccelerator(new KeyCodeCombination(KeyCode.BACK_SPACE));

        final Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(
                undoItem,
                redoItem,
                new SeparatorMenuItem(),
                cutItem,
                copyItem,
                pasteItem,
                duplicateItem,
                new SeparatorMenuItem(),
                deleteItem
        );
        return editMenu;
    }

    private static Menu createInsertMenu(Plugboard pb) {
        final Menu generators = new Menu("Generators");
        MenuItem sineSquare = new MenuItem("Sine/Square Oscillator");
        sineSquare.setAccelerator(new KeyCodeCombination(KeyCode.S));
        MenuItem noise = new MenuItem("Noise Generator");
        //noise.setAccelerator(new KeyCodeCombination(KeyCode.N));
        MenuItem lfo = new MenuItem("Low Frequency Oscillator");
        lfo.setAccelerator(new KeyCodeCombination(KeyCode.L));
        MenuItem player = new MenuItem("Clip Player");
        player.setAccelerator(new KeyCodeCombination(KeyCode.P));
        generators.getItems().addAll(sineSquare, noise, lfo, player);

        final Menu triggers = new Menu("Triggers");
        MenuItem noteSeq = new MenuItem("Note Sequencer");
        noteSeq.setAccelerator(new KeyCodeCombination(KeyCode.N));
        MenuItem drumSeq = new MenuItem("Drum Sequencer");
        drumSeq.setAccelerator(new KeyCodeCombination(KeyCode.D));
        triggers.getItems().addAll(noteSeq, drumSeq);

        final Menu effects = new Menu("Effects");
        //MenuItem noteSeq = new MenuItem("Note Sequencer");
        //noteSeq.setAccelerator(new KeyCodeCombination(KeyCode.N));
        //MenuItem drumSeq = new MenuItem("Drum Sequencer");
        //drumSeq.setAccelerator(new KeyCodeCombination(KeyCode.D));
        effects.getItems().addAll();

        final Menu io = new Menu("I/O");
        MenuItem inputsModule = new MenuItem("System Inputs");
        MenuItem outputsModule = new MenuItem("System Outputs");
        io.getItems().addAll(inputsModule, outputsModule);

        final Menu displays = new Menu("Displays");
        MenuItem valueDisp = new MenuItem("Display Value");
        valueDisp.setAccelerator(new KeyCodeCombination(KeyCode.V));
        MenuItem grapher = new MenuItem("Grapher Module");
        grapher.setAccelerator(new KeyCodeCombination(KeyCode.G));
        MenuItem oscilloscope = new MenuItem("Oscilloscope");
        oscilloscope.setAccelerator(new KeyCodeCombination(KeyCode.O));
        displays.getItems().addAll(valueDisp, grapher, oscilloscope);

        final Menu utilities = new Menu("Utilities");
        MenuItem mixer = new MenuItem("Mixer");
        mixer.setAccelerator(new KeyCodeCombination(KeyCode.M));
        MenuItem knob = new MenuItem("Knob");
        mixer.setAccelerator(new KeyCodeCombination(KeyCode.K));
        utilities.getItems().addAll(mixer, knob);

        final Menu insertMenu = new Menu("Insert");
        insertMenu.getItems().addAll(
                generators,
                triggers,
                effects,
                io,
                displays,
                utilities
        );
        return insertMenu;
    }

    private static Menu createHelpMenu() {
        MenuItem docs = new MenuItem("Documentation");
        docs.setDisable(true); // lol no docs

        final Menu helpMenu = new Menu("Help");
        helpMenu.getItems().addAll(docs);

        return helpMenu;
    }
}
