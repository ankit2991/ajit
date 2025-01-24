package com.messaging.textrasms.manager.common.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UtilsData {

    private static ProgressDialog progressDialog;

    public static void downloadZipFile(String urlStr, String destinationFilePath, Context context) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);

            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d("downloadZipFile", "Server ResponseCode=" + connection.getResponseCode() + " ResponseMessage=" + connection.getResponseMessage());
            }

            // download the file
            input = connection.getInputStream();

            Log.d("downloadZipFile", "destinationFilePath=" + destinationFilePath);
            new File(destinationFilePath).createNewFile();
            output = new FileOutputStream(destinationFilePath);

            byte[] data = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("downloadZipFile", "downloadZipFile: " + e.getMessage());
            return;
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("downloadZipFile", "downloadZipFile: " + e.getMessage());
            }

            if (connection != null) connection.disconnect();
        }

        File f = new File(destinationFilePath);

        Log.d("downloadZipFile", "f.getParentFile().getPath()=" + f.getParentFile().getAbsolutePath());
        Log.d("downloadZipFile", "f.getName()=" + f.getName().replace(".zip", ""));
        //unpackZip(f.getParentFile().getPath(), f.getName().replace(".zip", ""));
        unzip(context, f, new File(f.getAbsolutePath().replace(".zip", "")), f.getName().replace(".zip", ""));

    }

    public static void unzip(Context context, File zipFile, File targetDirectory, String name) {
        try (FileInputStream fis = new FileInputStream(zipFile)) {
            try (BufferedInputStream bis = new BufferedInputStream(fis)) {
                try (ZipInputStream zis = new ZipInputStream(bis)) {
                    ZipEntry ze;
                    int count;
                    byte[] buffer = new byte[8192];
                    while ((ze = zis.getNextEntry()) != null) {
                        String namenew = "";
                        if (ze.getName().startsWith(name)) {
                            namenew = ze.getName().replace(name, "");
                        } else {
                            namenew = ze.getName();

                        }
                        Log.d("downloadZipFile", "unzip: " + ze.getName() + "???" + namenew);
                        File file = new File(targetDirectory, namenew);
                        File dir = ze.isDirectory() ? file : file.getParentFile();
                        if (!dir.isDirectory() && !dir.mkdirs())
                            throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
                        if (ze.isDirectory())
                            continue;
                        try (FileOutputStream fout = new FileOutputStream(file)) {

                            while ((count = zis.read(buffer)) != -1)
                                fout.write(buffer, 0, count);
                        }
                        Log.d("downloadZipFile", "f.getParentFile().getPath()=" + ze.getName() + ">>>" + file.getAbsolutePath());

                    }

                }

            }
        } catch (Exception ex) {
            //handle exception
        }
    }

    public static boolean isContextActive(Context context) {
        if (null != context)
            if (context instanceof Activity) {
                return true;
            } else {
                return true;
            }
        return false;
    }

    public static void showProgressDialog(Context context, int resID) {
        try {
            if (isContextActive(context)) {
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setMessage(context.getString(resID));
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setIndeterminate(false);
                    if (!progressDialog.isShowing()) {
                        progressDialog.show();
                    }
                } else {
                    progressDialog.setMessage(context.getString(resID));
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setIndeterminate(false);

                    if (!progressDialog.isShowing()) {
                        progressDialog.show();
                    }
                }
            }
        } catch (Throwable throwable) {
            Log.i("TAG", "showProgressBar: throwable :- " + throwable.getMessage());
        }
    }

    public static void hideProgressDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            Log.i("TAG", "hideProgressDialog: error :- " + e.getMessage());
        }
        progressDialog = null;
    }
}