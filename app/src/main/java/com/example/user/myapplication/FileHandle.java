package com.example.user.myapplication;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

public class FileHandle
{
    int m_NumByteToRead;
    long m_FileLen;
    File file;
    byte[] bytes;

    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;

    //Constructor
    FileHandle(String FilePath)
    {
        file = new File(FilePath);
        m_FileLen = file.length();
        bytes = new byte[(int)m_FileLen];
    }

    long getFileSize()
    {
        return m_FileLen;
    }
    //**************************************************
    //  Function    : readFile
    //  Description : Read file into byte
    //**************************************************
    void readFile(byte[] byteRead)
    {
        try
        {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bufInputStream= new BufferedInputStream(fis);

            m_NumByteToRead = bufInputStream.available();
            bufInputStream.read(byteRead,0,byteRead.length); //Copy byte
            bufInputStream.close(); //Close file
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    void CheckExternalState()
    {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        }
        else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
        {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        }
        else
        {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
    }

    Boolean IsExternalStorageAvailable()
    {
        return this.mExternalStorageAvailable;
    }

    Boolean IsExternalStorageWriteable()
    {
        return this.mExternalStorageWriteable;
    }
}
