package com.example.hitcalc.utility;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ErrorMessageTracker {
    public void appendLog(String text)    {
        String contents[];
        String storageState = Environment.getExternalStorageState();
        //Add code to tailor what happens if the user has external media:
        if (Environment.MEDIA_MOUNTED.equals(storageState)) {
            String state = "mounted";
        }

        File sdcard = Environment.getExternalStorageDirectory();
        String path = sdcard.getAbsolutePath() + "/hit-calc/";
        File dir = new File(path);
        File root_dir = new File(sdcard.getAbsolutePath());
        // create this directory if not already created

        try
        {
            dir.mkdir();
            contents = root_dir.list();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (Error e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        File logFile = new File(dir,"combat-calc-log.file");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf. flush();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
