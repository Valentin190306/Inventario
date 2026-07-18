package com.casadelosol.inventario;

import com.casadelosol.inventario.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.getInstance().initialize();
        DataSeeder.seed();

        MainView mainView = new MainView();

        Scene scene = new Scene(mainView, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        primaryStage.setTitle("Inventario - Casa Del Sol");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
