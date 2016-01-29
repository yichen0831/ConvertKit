package com.ychstudio;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ConvertKit extends Application {

    String version = "v0.1";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent fxmlRoot = FXMLLoader.load(getClass().getClassLoader().getResource("ConvertKitApplication.fxml"));

        Scene scene = new Scene(fxmlRoot);

        primaryStage.setScene(scene);
        primaryStage.setTitle("ConvertKit " + version);
        primaryStage.show();

    }
}
