package com.example.billnbox;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the login FXML
            Parent root = FXMLLoader.load(getClass().getResource("1-login-page.fxml"));

            // Create the scene with the loaded FXML
            Scene scene = new Scene(root);

            // Set the scene on the primary stage
            primaryStage.setScene(scene);
            primaryStage.setTitle("Bill N Box");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
