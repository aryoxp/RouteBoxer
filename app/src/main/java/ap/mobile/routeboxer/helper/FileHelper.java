package ap.mobile.routeboxer.helper;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by aryo on 13/6/16.
 */
public class FileHelper {
    public static final String applicationDirectory = "RouteBoxer";
    public static final String trackCacheFile = "cache.track";
    private static String getTrackCacheFileName(Context context) {
        String externalDir = context.getExternalFilesDir(null).getAbsolutePath()
                + File.separator + applicationDirectory
                + File.separator + trackCacheFile;
        return externalDir;
    }

    public boolean createDir(String dirname) {
        String externalStorageDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        File basePath = new File(externalStorageDirectory + File.separator + FileHelper.applicationDirectory);
        try {
            return basePath.exists() || basePath.mkdir();
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean exists(Context context, String filename) throws IOException {
        String externalStorageDirectory = context.getExternalFilesDir(null).getAbsolutePath();
        File fileBasePath = new File(externalStorageDirectory
                + File.separator + FileHelper.applicationDirectory + File.separator + filename);
        if(fileBasePath.exists() && fileBasePath.canRead()) {
            return true;
        }
        return false;
    }

    public static String read(Context context, String filename) throws IOException {
        String externalStorageDirectory = context.getExternalFilesDir(null).getAbsolutePath();
        File fileBasePath = new File(externalStorageDirectory
                + File.separator + FileHelper.applicationDirectory + File.separator + filename);
        if(fileBasePath.exists() && fileBasePath.canRead()) {
            FileReader fileReader = new FileReader(fileBasePath);
            FileInputStream fileInputStream = new FileInputStream(fileBasePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            StringBuilder dataString = new StringBuilder();
            String buffer;
            while((buffer = bufferedReader.readLine()) != null) {
                dataString.append(buffer+"\n");
            }
            bufferedReader.close();
            fileInputStream.close();
            return dataString.toString();
        }
        return null;
    }

    public static Boolean write(Context context, String filename, String data) throws IOException {
        return FileHelper.write(context, filename, data, true);
    }

    public static Boolean write(Context context, String filename, String data, Boolean overwrite) throws IOException {
        String externalStorageDirectory = context.getExternalFilesDir(null).getAbsolutePath();
        File basePath = new File(externalStorageDirectory + File.separator + FileHelper.applicationDirectory);
        File fileBasePath = new File(externalStorageDirectory
                + File.separator + FileHelper.applicationDirectory
                + File.separator + filename);
        if(!basePath.exists())
            basePath.mkdirs();
        FileWriter fileWriter;

        if(fileBasePath.exists() && !overwrite)
            fileWriter = new FileWriter(fileBasePath.getAbsolutePath(), true); // append
        else fileWriter = new FileWriter(fileBasePath.getAbsolutePath());

        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(data);
        bufferedWriter.close();
        return true;
    }
}
