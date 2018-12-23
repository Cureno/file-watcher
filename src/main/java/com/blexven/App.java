package com.blexven;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private MainController mainController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {


        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Main.fxml"));
        VBox root = fxmlLoader.load();

        mainController = fxmlLoader.getController();
        mainController.launchWatcher();

        stage.setScene(new Scene(root, 400, 400));

        stage.setWidth(500);
        stage.setHeight(500);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        mainController.stopWatcher();
        super.stop();
    }
}
