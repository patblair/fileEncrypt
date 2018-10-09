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
    private File file = null;
    private String password;

    @Override
    public void start(Stage stage) throws Exception {

        final String NOFILE = "Error: No file.";
        final String NOPASS = "Error: No password.";
        final String NODECRYPT = "Error: File is not a .fenc or .tenc file";

        FileChooser fc = new FileChooser();
        fc.setTitle("Choose a file");

        Button browseButton = new Button("Browse...");
        Button encryptButton = new Button("Encrypt");
        Button decryptButton = new Button("Decrypt");

        Label fileLabel = new Label("File:");
        Label passLabel = new Label("Password:");
        Label errorLabel = new Label("Choose a file to encrypt/decrypt.");

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
         * Error label
         */
        gridPane.add(errorLabel, 0, 2, 3, 1);
        /*
         * Row 3
         * Choosing to encrypt or decrypt
         */
        gridPane.add(encryptButton, 0, 3);
        gridPane.add(decryptButton, 1, 3);
        /*
         * Row 4-7
         * soon
         */


        /*
         * Open file chooser
         */
        browseButton.setOnAction(e -> {
            file = fc.showOpenDialog(stage);
            if (file == null) {
                fileTextField.setText(null);
            } else {
                fileTextField.setText(file.getName());
            }
        });
        /*
         * Encrypts the file, as long as:
         * - The file exists
         * - A password was entered
         */
        encryptButton.setOnAction(e -> {
            if (file == null || !file.exists()) {
                errorLabel.setText(NOFILE);
            } else if (passTextField.getText().equals("")) {
                errorLabel.setText(NOPASS);
            } else {
                Data data = new Data(file, passTextField.getText());
                try {
                    data.encrypt();
                } catch (Exception el) {
                    el.printStackTrace();
                }
            }
        });
        /*
         * Decrypts file, as long as:
         * - The file exists
         * - The file is of the correct filetype (.fenc or .tenc)
         * - A password was entered
         * - The password is correct (TODO)
         *
         */
        decryptButton.setOnAction(e -> {
            if (file == null || !file.exists()) {
                errorLabel.setText(NOFILE);
            } else if (fileTextField.getLength() < 5 ||
                    !fileTextField.getText(fileTextField.getLength() - 5, fileTextField.getLength()).equals(".fenc") ||
                    !fileTextField.getText(fileTextField.getLength() - 5, fileTextField.getLength()).equals(".tenc")) {
                errorLabel.setText(NODECRYPT);
            } else if (passTextField.getText().equals("")) {
                errorLabel.setText(NOPASS);
            } else {
                Data data = new Data(file, passTextField.getText());
                try {
                    data.decrypt();
                } catch (Exception el) {
                    el.printStackTrace();
                }
            }
        });
        /*
         * Setup window
         */
        Scene scene = new Scene(gridPane, 270, 120);
        stage.setTitle("File Encrypt");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String args[]) {
        launch(args);
    }
}
