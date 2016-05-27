package ru.yaklimenko.fuel.utils;

import android.os.Environment;

import java.io.File;

import ru.yaklimenko.fuel.FuelApplication;

/**
 * Created by Антон on 25.05.2016.
 * storage routine
 */
public class StorageUtil {

    public static File getFilesDir() {
        String state = Environment.getExternalStorageState();
        File path;
        FuelApplication app = FuelApplication.instance;

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            path = app.getExternalFilesDir(null);
        } else {
            path = app.getFilesDir();
        }

        return createIfNotExists(path);
    }

    public static File createIfNotExists(File dir) {
        if (dir == null) {
            return null;
        }

        if (!dir.exists() && !dir.mkdir()) {
            return null;
        }
        return dir;
    }

}
