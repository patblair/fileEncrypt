package com.pat.file.encrypt;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {
    private File file;
    private String password;

    @Override
    public void start(Stage stage) throws Exception {

        FileChooser fc = new FileChooser();
        fc.setTitle("Choose a file");

        Button browseButton = new Button("Browse...");
        Button encryptButton = new Button("Encrypt");
        Button decryptButton = new Button("Decrypt");

        Label fileLabel = new Label("File:");
        Label passLabel = new Label("Password:");

        TextField passTextField = new TextField();
        TextField fileTextField = new TextField();
        fileTextField.setDisable(true);

        GridPane gridPane = new GridPane();
        /*
         * Row 0: File selection
         */
        gridPane.add(fileLabel, 0, 0);
        gridPane.add(fileTextField, 1, 0);
        gridPane.add(browseButton, 2, 0);
        /*
         * Row 1: Setting a password for the encrypted file, or
         * entering a password to attempt decryption
         */
        gridPane.add(passLabel, 0, 1);
        gridPane.add(passTextField, 1, 1);
        /*
         * Row 2
         * Choosing to encrypt or decrypt
         */
        gridPane.add(encryptButton, 0, 2);
        gridPane.add(decryptButton, 1, 2);
        /*
         * Open file chooser
         */
        browseButton.setOnAction(e -> {
            file = fc.showOpenDialog(stage);
            fileTextField.setText(file.getName());
        });
        /*
         * Attempt to encrypt file
         */
        encryptButton.setOnAction(e -> {
            if (passTextField.getText().equals("") || passTextField.getText().equals("Enter a password!")) {
                passTextField.setText("Enter a password!");
            } else {
                Data data = new Data(file, passTextField.getText());
                try {
                    data.encrypt();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        /*
         * Attempt to decrypt file
         */
        decryptButton.setOnAction(e -> {
            if (passTextField.getText().equals("") || passTextField.getText().equals("Enter a password!")) {
                passTextField.setText("Enter a password!");
            } else {
                Data data = new Data(file, passTextField.getText());
                try {
                    data.decrypt();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        Scene scene = new Scene(gridPane, 270, 80);
        stage.setTitle("File Encrypt");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String args[]) {
        launch(args);
    }
}
