package com.example.user.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

//*******************************************
//  1) Read File
//  2) Encrypt File

public class ActivityEncrypt extends Activity
{
    private static String algorithm = "AES";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_encrypt);
        Toast.makeText(ActivityEncrypt.this,"ActivityEncrypt - started ",Toast.LENGTH_LONG).show();
    }

    //Generate Salt
    public static SecretKey generateSalt()
            throws NoSuchAlgorithmException
    {
        SecureRandom secureRandom = new SecureRandom();
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256,secureRandom);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    //Generate Secret key using password and salt
    public static SecretKey GenerateKey(char[] password, byte[] salt)
            throws NoSuchAlgorithmException,InvalidKeySpecException
    {
        final int iterations = 1000;
        final int outputKeyLength = 256;
        SecretKeyFactory secreteKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(password,salt,iterations,outputKeyLength);
        SecretKey secretKeySet = secreteKeyFactory.generateSecret(keySpec);
        return secretKeySet;
    }

    //encrypt file
    public byte[] encodeFile(SecretKey secretKeySet,byte[] fileContents)
            throws Exception
    {
        byte[] data = secretKeySet.getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(data,0,data.length,algorithm);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);

        byte[] encryptedData = cipher.doFinal(fileContents);
        return encryptedData;
    }

    public byte[] decodeFile(SecretKey secretKeySet, byte[] fileContents)
            throws Exception
    {
        byte[] data = secretKeySet.getEncoded();
        SecretKeySpec skeySpec = new SecretKeySpec(data, 0, data.length,algorithm);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);

        byte[] decrypted = cipher.doFinal(fileContents);
        return decrypted;
    }



}
