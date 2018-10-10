package com.pat.file.encrypt;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {
    private File file = null;

    @Override
    public void start(Stage stage) throws Exception {

        final String NOFILE = "Error: No file.\n ";
        final String NOPASS = "Error: No password.\n ";
        final String NODECRYPT = "Error: File is not a .fenc file\n ";

        FileChooser fc = new FileChooser();
        fc.setTitle("Choose a file");

        Button browseButton = new Button("Browse...");
        Button encryptButton = new Button("Encrypt");
        Button decryptButton = new Button("Decrypt");

        Label fileLabel = new Label("File:");
        Label passLabel = new Label("Password:");
        Label infoLabel = new Label("Choose a file to encrypt/decrypt.\n ");

        TextField passTextField = new TextField();
        TextField fileTextField = new TextField();
        fileTextField.setDisable(true);

        GridPane fileEncMain = new GridPane();
        /*
         * Row 0: File selection
         */
        fileEncMain.add(fileLabel, 0, 0);
        fileEncMain.add(fileTextField, 1, 0);
        fileEncMain.add(browseButton, 2, 0);
        /*
         * Row 1: Setting a password for the encrypted file, or
         * entering a password to attempt decryption
         */
        fileEncMain.add(passLabel, 0, 1);
        fileEncMain.add(passTextField, 1, 1);
        /*
         * Row 2
         * Error label
         */
        fileEncMain.add(infoLabel, 0, 2, 3, 1);
        /*
         * Row 3
         * Choosing to encrypt or decrypt
         */
        fileEncMain.add(encryptButton, 0, 3);
        fileEncMain.add(decryptButton, 1, 3);

        /*
         * Open file chooser
         */
        browseButton.setOnAction(e -> {
            file = fc.showOpenDialog(stage);
            if (file == null) {
                fileTextField.setText(null);
            } else {
                fileTextField.setText(file.getName());
                infoLabel.setText("Now enter a password to encrypt/decrypt with.\n ");
            }
        });
        /*
         * Encrypts the file, as long as:
         * - The file exists
         * - A password was entered
         */
        encryptButton.setOnAction(e -> {
            if (file == null || !file.exists()) {
                infoLabel.setText(NOFILE);
            } else if (passTextField.getText().equals("")) {
                infoLabel.setText(NOPASS);
            } else {
                Data data = new Data(file, passTextField.getText());
                try {
                    if(data.encrypt()) {
                        infoLabel.setText("Encryption success, file saved to: \n"+file.getParent());
                    } else {
                        infoLabel.setText("Encryption failure.\n ");
                    }
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
         * - TODO: Detect if the password is incorrect instead of just failing
         */
        decryptButton.setOnAction(e -> {
            if (file == null || !file.exists()) {
                infoLabel.setText(NOFILE);
            } else if (fileTextField.getLength() < 5 ||
                    !fileTextField.getText(fileTextField.getLength() - 5, fileTextField.getLength()).equals(".fenc")) {
                infoLabel.setText(NODECRYPT);
            } else if (passTextField.getText().equals("")) {
                infoLabel.setText(NOPASS);
            } else {
                Data data = new Data(file, passTextField.getText());
                try {
                    if(!data.decrypt()) {
                        infoLabel.setText("Decryption failure\n "); //not yet implemented
                    } else {
                        infoLabel.setText("Decryption success, file saved to: \n"+file.getParent());
                    }
                } catch (Exception el) {
                    el.printStackTrace();
                }
            }
        });
        /*
         * Setup window
         */
        Scene scene = new Scene(fileEncMain, 267, 110);
        stage.setTitle("File Encrypt");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String args[]) {
        launch(args);
    }
}
