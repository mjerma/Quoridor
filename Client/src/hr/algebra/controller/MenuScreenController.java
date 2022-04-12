package hr.algebra.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MenuScreenController {

    @FXML
    public Button btnOffline;
    @FXML
    public Button btnOnline;

    public void playOnline(){
        try {
            Stage stage = (Stage) btnOnline.getScene().getWindow();
            stage.close();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../view/GameScreen.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage = new Stage();
            stage.setTitle("Quoridor online");
            stage.setScene(scene);
            stage.setOnCloseRequest(e->
                    System.exit(0));
            GameScreenController controller = fxmlLoader.getController();
            stage.setOnShown((WindowEvent event) -> {
                try {
                    controller.initClientThread();
                } catch (IOException ex) {
                    Logger.getLogger(MenuScreenController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            stage.show();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void playOffline() {
        try {
            Stage stage = (Stage) btnOffline.getScene().getWindow();
            stage.close();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../view/GameScreen.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage = new Stage();
            stage.setTitle("Quoridor offline");
            stage.setScene(scene);
            stage.setOnCloseRequest(e->
                    System.exit(0));
            GameScreenController controller = fxmlLoader.getController();
            stage.setOnShown((WindowEvent event) -> controller.startClockThread());
            stage.show();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
