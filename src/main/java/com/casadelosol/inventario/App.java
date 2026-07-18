package com.casadelosol.inventario;

import com.casadelosol.inventario.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.getInstance().initialize();

        MainView mainView = new MainView();

        Scene scene = new Scene(mainView, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        primaryStage.setTitle("Inventario - Casa Del Sol");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
