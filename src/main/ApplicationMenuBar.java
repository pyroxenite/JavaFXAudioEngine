package main;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class ApplicationMenuBar extends MenuBar {
    public ApplicationMenuBar() {
        super();

        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac"))
            this.useSystemMenuBarProperty().set(true);

        this.getMenus().addAll(
                createFileMenu(),
                createEditMenu(),
                createHelpMenu()
        );

    }

    private Menu createHelpMenu() {
        final Menu helpMenu = new Menu("Help");

        return helpMenu;
    }

    private Menu createEditMenu() {
        final String os = System.getProperty("os.name");
        KeyCombination.Modifier ctrl;
        if (os != null && os.startsWith("Mac"))
            ctrl = KeyCodeCombination.META_DOWN;
        else
            ctrl = KeyCodeCombination.CONTROL_DOWN;

        MenuItem undoItem = new MenuItem("Undo");
        undoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, ctrl));

        MenuItem redoItem = new MenuItem("Redo");
        redoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, ctrl, KeyCombination.SHIFT_DOWN));

        MenuItem cutItem = new MenuItem("Cut");
        cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X, ctrl));

        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, ctrl));

        MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, ctrl));

        MenuItem duplicateItem = new MenuItem("Duplicate");
        duplicateItem.setAccelerator(new KeyCodeCombination(KeyCode.D, ctrl));

        final Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(
                undoItem,
                redoItem,
                new SeparatorMenuItem(),
                cutItem,
                copyItem,
                pasteItem,
                duplicateItem
        );
        return editMenu;
    }

    private Menu createFileMenu() {
        final String os = System.getProperty("os.name");
        KeyCombination.Modifier ctrl;
        if (os != null && os.startsWith("Mac"))
            ctrl = KeyCodeCombination.META_DOWN;
        else
            ctrl = KeyCodeCombination.CONTROL_DOWN;

        MenuItem newItem = new MenuItem("New plugboard...");
        newItem.setAccelerator(new KeyCodeCombination(KeyCode.N, ctrl));

        MenuItem openItem = new MenuItem("Open...");
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, ctrl));

        MenuItem saveItem = new MenuItem("Save");
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, ctrl));

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
}
