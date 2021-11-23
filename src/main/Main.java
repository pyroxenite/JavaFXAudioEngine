package main;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import main.modules.OutputsModule;

import java.awt.desktop.AppForegroundListener;

public class Main extends Application {
    private Plugboard plugboard;

    public void start(Stage stage) {
        try {
            BorderPane root = new BorderPane();
            Scene scene = new Scene(root,1500,800);

            System.out.println("Test");

            plugboard = new Plugboard(scene.getWidth(), scene.getHeight(), stage);
            root.setCenter(plugboard);

            root.setTop(new ApplicationMenuBar(plugboard));

            setSingleKeyShortcuts(scene, plugboard);

            stage.setScene(scene);
            stage.setTitle("Little Endian Plugboard");
            stage.show();

            plugboard.widthProperty().bind(root.widthProperty());
            plugboard.heightProperty().bind(root.heightProperty());

            new AnimationTimer() {
                private long lastUpdate = 0 ;
                @Override
                public void handle(long now) {
                    if (now - lastUpdate >= 32_000_000) {
                        plugboard.redraw();
                        lastUpdate = now;
                    }
                }
            }.start();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setSingleKeyShortcuts(Scene scene, Plugboard plugboard) {
        scene.setOnKeyPressed(e -> {
            if (e.getText().compareTo(" ") == 0) {
                //plugboard.togglePlayback();
            } else if (e.getCode() == KeyCode.BACK_SPACE) {
                plugboard.deleteSelectedModule();
            }

        });
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