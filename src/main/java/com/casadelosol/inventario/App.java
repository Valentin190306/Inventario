package com.casadelosol.inventario;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.getInstance().initialize();

        StackPane root = new StackPane();
        root.getChildren().add(new Label("Casa Del Sol - Inventario"));

        Scene scene = new Scene(root, 1024, 768);

        primaryStage.setTitle("Inventario - Casa Del Sol");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
