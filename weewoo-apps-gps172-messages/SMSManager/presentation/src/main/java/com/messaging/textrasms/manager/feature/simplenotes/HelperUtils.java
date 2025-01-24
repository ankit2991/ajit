package com.messaging.textrasms.manager.feature.simplenotes;

import android.content.Context;
import android.widget.Toast;

import com.messaging.textrasms.manager.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class HelperUtils {

    public static String TEXT_FILE_EXTENSION = ".txt";
    public static String PREFERENCE_COLOUR_FONT = "colourFont";

    public static ArrayList<File> getFiles(Context context) {
        File[] files = context.getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(HelperUtils.TEXT_FILE_EXTENSION);
            }
        });
        Arrays.sort(files, (file1, file2) -> Long.valueOf(file2.lastModified()).compareTo(file1.lastModified()));
        return new ArrayList<>(Arrays.asList(files));
    }

    public static boolean fileExists(Context context, String fileName) {
        File file = context.getFileStreamPath(fileName + HelperUtils.TEXT_FILE_EXTENSION);
        return file.exists();
    }

    public static void writeFile(Context context, String fileName, String fileContent) {
        try {
            OutputStreamWriter out = new OutputStreamWriter(context.openFileOutput(fileName + HelperUtils.TEXT_FILE_EXTENSION, 0));
            out.write(fileContent);
            out.close();
        } catch (Throwable t) {
            Toast.makeText(context, context.getString(R.string.exception) + t, Toast.LENGTH_LONG).show();
        }
    }

    public static String readFile(Context context, String fileName) {
        String content = "";
        if (fileExists(context, fileName)) {
            try {
                InputStream in = context.openFileInput(fileName + HelperUtils.TEXT_FILE_EXTENSION);
                if (in != null) {
                    InputStreamReader tmp = new InputStreamReader(in);
                    BufferedReader reader = new BufferedReader(tmp);
                    String str;
                    StringBuilder buf = new StringBuilder();
                    while ((str = reader.readLine()) != null) {
                        buf.append(str).append("\n");
                    }
                    in.close();
                    content = buf.toString();
                }
            } catch (Exception e) {
                //  Toast.makeText(context, context.getString(R.string.exception) + e.toString(), Toast.LENGTH_LONG).show();
            }
        }
        return content.trim();
    }

}
