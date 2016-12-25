package com.example.user.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

//**************************************************
//Class        : DisplayFile
//description  : this class display the file contents
//and give options to user for encryption and decryption
//**************************************************
public class DisplayFile extends AppCompatActivity
{
    FileHandle handle;              //File handle for file to be read
    String pathName = null;         //path to the file
    SecretKey mSecreteKeySalt = null; //Salt to generate secret key. [Will be keep in saveState]
    SecretKey mSecreteKey = null;     //Secrete key

    byte[] filecontent;             //Buffer to keep the file content from reading
    byte[] filecontent_ciper;       //This is needed until we sucessfully write ciper to file
    byte[] filecontent_restore;
    String password;
    String TextToDisplay;           //Text to be display in textView. [Will be keep in saveState]
    Intent intent;

    byte[] saltInByte;              //32bytes

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_file);

        //get pathName
        if (savedInstanceState == null)
        {
            //-----Fresh launch-----

            //get the variable passed from caller
            Bundle extras = getIntent().getExtras();
            if (extras != null)
            {
                pathName = extras.getString("PathName");
            }

            handle = new FileHandle(pathName);                      //Read file using file path
            initializeBufferSize((int) handle.getFileSize());        //initialize the buffer size
            handle.readFile(filecontent);

            TextToDisplay = new String(filecontent);
        }
        else
        {
            //relaunch probably due to the screen orientation change and cause OnCreate and onDestroy is called.
            //restore the onSaveinstantState
            pathName = (String) savedInstanceState.getSerializable("PathName");
            TextToDisplay = (String) savedInstanceState.getSerializable("TextToDisplay");

            //Convert from byte[] to SecretKey
            mSecreteKeySalt = new SecretKeySpec(savedInstanceState.getByteArray("SaltInByte"),
                    0,
                    savedInstanceState.getByteArray("SaltInByte").length,
                    "DES");
        }

        //Display to TextView
        TextView textView = (TextView) findViewById(R.id.TextView_fileContent);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setText(TextToDisplay);
    }

    //----------------------------------------------------------------------------------------------
    //Function    : Encrypt_clicked
    //Description : on-click event for the "Encryption" button in displayFile activity
    //Invoke PasswordDialog dialog to get password from user
    //Encryption will be carry out at onActivityResult
    //----------------------------------------------------------------------------------------------
    public void Encrypt_clicked(View view) {
        intent = new Intent(this, PasswordDialog.class);
        startActivityForResult(intent, 1);
    }

    //----------------------------------------------------------------------------------------------
    //Function    : Encrypt_clicked
    //Description : on-click event for the "Decryption" button in displayFile activity
    //Invoke PasswordDialog dialog to get password from user
    //Decryption will be carry out at onActivityResult
    //----------------------------------------------------------------------------------------------
    public void Decrypt_clicked(View view) {
        intent = new Intent(this, PasswordDialog.class);
        startActivityForResult(intent, 2);
    }

    //----------------------------------------------------------------------------------------------
    //Function    : onActivityResult
    //Description : startActivityForResult will invoke new activity
    //new activity set the result
    //This onActivityResult will be called to retrieve the result value
    //----------------------------------------------------------------------------------------------
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1: // Encrypt_clicked->PasswordDialog->requestCode:1
            {
                if (resultCode == RESULT_OK) {
                    password = data.getStringExtra("Userpassword");
                    Encryption(password);
                }
                break;
            }

            case 2: // Decrypt_clicked->PasswordDialog->requestCode:1
            {
                if (resultCode == RESULT_OK) {
                    password = data.getStringExtra("Userpassword");
                    Decryption(password);
                }
                break;
            }

            default:
                break;
        }
    }

    //----------------------------------------------------------------------------------------------
    //  Function    : Encryption
    //  Description : 1) generate salt using user defined password
    //                2) generate key using salt
    //                3) encrypt filecontent using key
    //                4) filecontent_ciper as encryption result
    //----------------------------------------------------------------------------------------------
    public void Encryption(String Password)
    {
        ActivityEncrypt activityEncrypt = new ActivityEncrypt();
        char[] passCode = Password.toCharArray();

        try
        {
            mSecreteKeySalt = activityEncrypt.generateSalt();   //generate salt
            saltInByte = mSecreteKeySalt.getEncoded();  //convert salt to byte

            mSecreteKey = activityEncrypt.GenerateKey(passCode, mSecreteKeySalt.toString().getBytes());  //generate key
            filecontent_ciper = activityEncrypt.encodeFile(mSecreteKey, filecontent);    //Encode file

            //Display Key and salt
            Log.e("Encryption SALT : ", Base64.encodeToString(mSecreteKeySalt.getEncoded(), Base64.DEFAULT));
            Log.e("SaltSize        : ", String.valueOf(saltInByte.length));
            Log.e("Encryption KEY  : ", Base64.encodeToString(mSecreteKey.getEncoded(), Base64.DEFAULT));

            //Display
            TextToDisplay = new String(filecontent_ciper);
            TextView textView = (TextView) findViewById(R.id.TextView_fileContent);
            textView.setMovementMethod(new ScrollingMovementMethod());
            textView.setText(TextToDisplay);

            Log.e("MSG : ","Start writefile");
            writeFile(saltInByte,filecontent_ciper);
            Log.e("MSG : ","End writefile");
        } catch (Exception e)
        {
            System.out.printf("Exception : %s \n", e.getMessage());
            System.out.printf("Line      : %d \n", Thread.currentThread().getStackTrace()[1].getLineNumber());
            System.out.printf("File      : %s \n", Thread.currentThread().getStackTrace()[1].getFileName());
        }
    }

    //----------------------------------------------------------------------------------------------
    //  Function    : Decryption
    //  Description : 1) salt is retain from encryption
    //                2) generate key using salt
    //                3) decrypt filecontent_ciper using key
    //                4) TextToDisplay as decryption result
    //----------------------------------------------------------------------------------------------
    public void Decryption(String Password) {
        ActivityEncrypt activityEncrypt = new ActivityEncrypt();
        char[] passCode = Password.toCharArray();

        try {
            SecretKey mSecreteKeyA = activityEncrypt.GenerateKey(passCode, mSecreteKeySalt.toString().getBytes());  //generate key
            filecontent_restore = activityEncrypt.decodeFile(mSecreteKeyA, filecontent_ciper);

            //Display
            TextToDisplay = new String(filecontent_restore);
            TextView textView = (TextView) findViewById(R.id.TextView_fileContent);
            textView.setMovementMethod(new ScrollingMovementMethod());
            textView.setText(TextToDisplay);
        } catch (Exception e) {
            System.out.printf("Exception : %s \n", e.getMessage());
            System.out.printf("Line      : %d \n", Thread.currentThread().getStackTrace()[1].getLineNumber());
            System.out.printf("File      : %s \n", Thread.currentThread().getStackTrace()[1].getFileName());
        }
    }

    //----------------------------------------------------------------------------------------------
    //  Function    : onSaveInstanceState
    //  Description : save all current state information and restore during onCreate
    //----------------------------------------------------------------------------------------------
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("PathName", pathName);
        outState.putSerializable("TextToDisplay", TextToDisplay);
        outState.putByteArray("SaltInByte", saltInByte);
    }

    //----------------------------------------------------------------------------------------------
    //  Function    : initializeBufferSize
    //  Description : initialize the buffer using file size
    //----------------------------------------------------------------------------------------------
    public void initializeBufferSize(int fileSize) {
        filecontent = new byte[fileSize];
        filecontent_ciper = new byte[fileSize];
        filecontent_restore = new byte[fileSize];
    }

    //----------------------------------------------------------------------------------------------
    //  Function    : writeFile
    //  Description : 1)Receive ciper and salt
    //                2)initialize write buffet with size of ciper + salt
    //
    //----------------------------------------------------------------------------------------------
    public void writeFile(byte[] salt, byte[] ciper)
    {
        char[] Charheader = {'E','N','C','R','Y','P','T','E','D'};
        byte[] Byteheader = toBytes(Charheader);


        byte[] ByteTowrite = new byte[Byteheader.length + salt.length + ciper.length];

        //[ENCRYPTED]
        System.arraycopy(Byteheader,
                0,
                ByteTowrite,
                0,
                Byteheader.length
        );
        //[ENCRYPTED][SALT]
        System.arraycopy(salt,
                0,
                ByteTowrite,
                Byteheader.length,
                salt.length
        );
        //[ENCRYPTED][SALT][CONTENT]
        System.arraycopy(ciper,
                0,
                ByteTowrite,
                Byteheader.length+salt.length,
                ciper.length
        );

        try
        {
            FileOutputStream FOS = new FileOutputStream(pathName, false);
            FOS.write(ByteTowrite);
        }
        catch(IOException e)
        {
            Log.e("Exception :",e.getMessage());
        }
    }

    //----------------------------------------------------------------------------------------------
    //  Function    : readFile
    //  Description : 1)Read file
    //                2)It can be a decrypted or encrypted by
    //
    //----------------------------------------------------------------------------------------------
    public void readFile(String mPathName,String mode)
    {
        char[] Charheader = {'E','N','C','R','Y','P','T','E','D'};
        byte[] HeaderToCompare = toBytes(Charheader);
        byte[] HeaderFromFile = new byte[HeaderToCompare.length];

        try
        {
            RandomAccessFile RAF = new RandomAccessFile(mPathName, mode);
            RAF.read(HeaderFromFile,0,HeaderToCompare.length);

            //check header
            if(Arrays.equals(HeaderToCompare,HeaderFromFile))
            {
                //-----This is Encrypted file-----



            }
            else
            {
                //-----This is un-Encrypted file-----
            }

        }
        catch(Exception e)
        {
            Log.e("Exception :",e.getMessage());
        }
    }

    private byte[] toBytes(char[] chars)
    {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }



}