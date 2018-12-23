package com.blexven;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    ListView<HBox> listView;
    private Watcher watcher = new Watcher();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initializer");

    }

    public void launchWatcher() {

        new Thread(
                () -> {
                    try {
                        watcher.watchJarDirectoryAndRunRegisteredCommands();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        ).start();

        new Thread(
                () -> watcher.getFilesAndCommands().forEach(
                        (fileName, command) -> Platform.runLater(
                                () -> {
                                    Text name = new Text((String) fileName);

                                    name.setStyle("-fx-font-weight: bold; -fx-font-size: 1.2em;");

                                    HBox item = new HBox(name, new Text((String) command));

                                    item.setSpacing(30);

                                    listView.getItems().add(item);
                                }
                        )
                )
        ).start();
    }

    public void stopWatcher() {
        watcher.stop();
    }
}
