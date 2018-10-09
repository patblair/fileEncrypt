package com.pat.file.encrypt;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

class Data {
    private File file;
    private String password;

    /**
     * A data object consists of a file, which will be encrypted/decrypted, and a user-specified
     * password, which will be used for encryption or (attempted) decryption
     *
     * @param file     - The file which will be encrypted or decrypted
     * @param password - The password to be used for encryption or (attempted decryption)
     */
    Data(File file, String password) {
        this.file = file;
        this.password = password;
    }

    /**
     * Getters and setters for the above object
     */
    void setFile(File file) {
        this.file = file;
    }

    private File getFile() {
        return file;
    }

    void setPassword(String password) {
        this.password = password;
    }

    String getPassword() {
        return password;
    }

    /**
     * Saves the encrypted file, salt, and initialization vector
     * to the same directory as the file that has been encrypted
     *
     * @throws Exception ...
     */
    void encrypt() throws Exception {
        /*
         * Input the file to be encrypted
         */
        FileInputStream in = new FileInputStream(file);
        /*
         * Encrypted file output
         */
        FileOutputStream out = new FileOutputStream(file.getParent() +
                "/" + file.getName() + ".aes");
        /*
         * Prepare for file encryption
         * Save salt as "salt.aes"
         */
        byte[] salt = new byte[8];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        FileOutputStream saltOutFile = new FileOutputStream(file.getParent() + "/salt.aes");
        saltOutFile.write(salt);
        saltOutFile.close();
        /*
         * Generate key with user-specified password
         */
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey secretKey = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = cipher.getParameters();
        /*
         * Save initialization vector as "iv.aes"
         */
        FileOutputStream ivOutFile = new FileOutputStream(file.getParent() + "/iv.aes");
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        ivOutFile.write(iv);
        ivOutFile.close();
        /*
         * Encrypt the file
         */
        byte[] input = new byte[64];
        int bytesRead;

        while ((bytesRead = in.read(input)) != -1) {
            byte[] output = cipher.update(input, 0, bytesRead);
            if (output != null)
                out.write(output);
        }

        byte[] output = cipher.doFinal();
        if (output != null)
            out.write(output);

        in.close();
        out.flush();
        out.close();

        System.out.println("File successfully encrypted.");
    }

    /**
     * Uses the saved salt and initialization vector, as well as
     * the user-input password to attempt file decryption
     *
     * @throws Exception ...
     */
    void decrypt() throws Exception {
        /*
         * Read salt
         */
        FileInputStream saltFis = new FileInputStream(file.getParent() + "/salt.aes");
        byte[] salt = new byte[8];
        saltFis.read(salt);
        saltFis.close();
        /*
         * Read initialization vector
         */
        FileInputStream ivFis = new FileInputStream(file.getParent() + "/iv.aes");
        byte[] iv = new byte[16];
        ivFis.read(iv);
        ivFis.close();
        /*
         * Generate key with user-specified password,
         * if password is incorrect decryption fails
         */
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        /*
         * Decrypt the file
         */
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        FileInputStream fis = new FileInputStream(file);
        /*
         * Save decrypted file in same directory as encrypted version
         */
        FileOutputStream fos = new FileOutputStream(file.getParent() + "/" +
                file.getName().substring(0, file.getName().length() - 4));
        byte[] in = new byte[64];
        int read;
        while ((read = fis.read(in)) != -1) {
            byte[] output = cipher.update(in, 0, read);
            if (output != null)
                fos.write(output);
        }

        byte[] output = cipher.doFinal();
        if (output != null)
            fos.write(output);
        fis.close();
        fos.flush();
        fos.close();
        System.out.println("File successfully decrypted.");
    }
}