/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class MessageUtils {
    
    public static void showAlert(String message, String title){
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.show();
    }

    public static void showError(String message, String title){
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.show();
    }
}
