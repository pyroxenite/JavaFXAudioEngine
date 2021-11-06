package main;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import main.modules.OutputsModule;

public class Main extends Application {
    private Plugboard plugboard;

    public void start(Stage primaryStage) {
        try {
            BorderPane root = new BorderPane();
            Scene scene = new Scene(root,1500,800);
            root.setTop(createToolbar());
            plugboard = new Plugboard(scene.getWidth(), scene.getHeight());
            root.setCenter(plugboard);
            primaryStage.setScene(scene);
            primaryStage.setTitle("The JavaFX audio processor");
            primaryStage.show();

            new AnimationTimer() {
                @Override
                public void handle(long l) {
                    plugboard.redraw();
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Node createToolbar() {
        Button button = new Button("appuyez !");
        ToolBar tb = new ToolBar(button, new Label("ceci est un label"), new Separator());
        button.setOnAction(event -> System.out.println("appui!"));
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll("Item 1", "Item 2", "Item 3");
        tb.getItems().add(cb);
        return tb;
    }

    private Node createStatusbar() {
        HBox statusbar = new HBox();
        statusbar.getChildren().addAll(new Label("Name:"), new TextField("    "));
        return statusbar;
    }

    @Override
    public void stop(){
        plugboard.getModules().forEach(module -> {
            if (module.getClass() == OutputsModule.class) {
                ((OutputsModule) module).stop();
            }
        });
    }
}