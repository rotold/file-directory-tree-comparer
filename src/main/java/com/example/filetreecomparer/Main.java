package com.example.filetreecomparer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// needed to use https://www.youtube.com/watch?v=WtOgoomDewo and https://www.youtube.com/watch?v=82QcFSstJs0
// for Intellij
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Directory Comparer");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
        // fileDB anal1=new fileDB();
        // anal1.setPathA(sample.Controller.getPatha()); cant do this because not static
    }
}
