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
     * test
     *
     * @throws Exception
     */
    void encrypt() throws Exception {
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
            if (output != null)
                out.write(output);
        }

        byte[] output = cipher.doFinal();
        if (output != null)
            out.write(output);
        /*
         * Embed salt and initialization vector at end of file
         * Last 16 bytes = iv, the 8 bytes before that = salt
         */
        in.close();
        out.flush();
        out.write(salt);
        out.write(iv);
        out.close();
        System.out.println("Encryption successful");
    }

    void decrypt() throws Exception {
        /*
         * Read salt
         */
        RandomAccessFile raf = new RandomAccessFile(file, "rw");

        byte[] salt = new byte[8];
        byte[] iv = new byte [16];

        raf.seek(file.length() - (salt.length + iv.length));
        raf.read(salt, 0, salt.length);

        raf.seek(file.length() - iv.length);
        raf.read(iv, 0, iv.length);

        raf.setLength(file.length() - (salt.length + iv.length));
        raf.close();

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        /*
         * Decrypt the file
         */
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        FileInputStream in = new FileInputStream(file);
        /*
         * Save decrypted file in same directory as encrypted version
         *
         * Remove the .fenc extension
         */
        FileOutputStream out = new FileOutputStream(file.getParent() + "/" +
                file.getName().substring(0, file.getName().length() - 5));
        byte[] b = new byte[64];
        int read;
        while ((read = in.read(b)) != -1) {
            byte[] output = cipher.update(b, 0, read);
            if (output != null)
                out.write(output);
        }

        byte[] output = cipher.doFinal();
        if (output != null)
            out.write(output);
        in.close();
        out.flush();
        out.close();
        /*
         * Delete encrypted file
         */
        if (file.delete()) {
            System.out.println("File deleted");
            System.out.println("Decryption successful");
        }
    }
    /**
     * Uses the saved salt and initialization vector, as well as
     * the user-input password to attempt file decryption
     *
     * @throws Exception ...
     */
    void oldDecrypt() throws Exception {
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
         *
         * Remove ".aes" file extension. If the encrypted file was not renamed,
         * it should now have the correct file extension and open easily
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
        file.delete();
    }
}
