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
import java.io.RandomAccessFile;
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
     * Encrypts the file with 256-bit AES encryption, using the user-entered password as the key.
     * After encryption, the salt and initialization vector are embedded at the end of the file.
     *
     * @return true if file encrypted successfully, or false if encryption fails.
     * @throws Exception ...
     */
    boolean encrypt() throws Exception {
        if (!file.exists() || password == null || file == null) {
            return false;
        }
        /*
         * Input the file to be encrypted
         */
        FileInputStream in = new FileInputStream(file);
        /*
         * Generate salt
         */
        byte[] salt = new byte[8];
        SecureRandom srand = new SecureRandom();
        srand.nextBytes(salt);
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
         * Generate initialization vector
         */
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        /*
         * Encrypted and output the file
         *
         * .fenc = fileEncrypt extension, means that the salt and
         * initialization vector are embedded at the end of the
         * encrypted file, respectively
         */
        FileOutputStream out = new FileOutputStream(file.getParent() +
                "/" + file.getName() + ".fenc");
        byte[] input = new byte[64];
        int bytesRead;
        while ((bytesRead = in.read(input)) != -1) {
            byte[] output = cipher.update(input, 0, bytesRead);
            if (output != null) {
                out.write(output);
            }
        }
        byte[] output = cipher.doFinal();
        if (output != null) {
            out.write(output);
        }
        /*
         * Finish up and embed salt and initialization vector at end of file
         * Last 24 bytes = salt and initialization vector (in that order)
         * File encryption successful, return true
         */
        in.close();
        out.flush();
        out.write(salt);
        out.write(iv);
        out.close();
        return true;
    }

    /**
     * Attempts to decrypt the .fenc file with the supplied password
     *
     * @return true if file decrypted successfully, or false if decryption failed
     * @throws Exception ...
     */
    boolean decrypt() throws Exception {
        /*
         * Read embedded salt and initialization vector
         */
        if (!file.exists() || password == null || file == null) {
            return false;
        }

        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        byte[] salt = new byte[8];
        byte[] iv = new byte[16];
        raf.seek(file.length() - (salt.length + iv.length));
        raf.read(salt, 0, salt.length);
        raf.seek(file.length() - (iv.length));
        raf.read(iv, 0, iv.length);
        raf.setLength(file.length() - (salt.length + iv.length));
        /*
         * Generate decryption key from password
         */
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        /*
         * Begin decryption
         */
        FileInputStream in = new FileInputStream(file);
        FileOutputStream out = new FileOutputStream(file.getParent() + "/" +
                file.getName().substring(0, file.getName().length() - 5));
        byte[] b = new byte[64];
        int read;
        while ((read = in.read(b)) != -1) {
            byte[] output = cipher.update(b, 0, read);
            if (output != null) {
                out.write(output);
            }
        }
        /*
         * Attempt to decrypt the file, return false if password is incorrect
         */
        try {
            byte[] output = cipher.doFinal();
            if (output != null)
                out.write(output);
        } catch (Exception e) {
            /*
             * Decryption failed
             * Re-embed salt/iv
             * Return false
             */
            raf.seek(file.length());
            raf.write(salt);
            raf.seek(file.length());
            raf.write(iv);
            raf.close();
            return false;
        }
        /*
         * Dencrypion successful
         * Re-embed salt/iv in encrypted file
         * Finish up decryption and close streams/raf
         * Return true
         */
        raf.seek(file.length());
        raf.write(salt);
        raf.seek(file.length());
        raf.write(iv);
        raf.close();
        in.close();
        out.flush();
        out.close();
        return true;
    }
}
