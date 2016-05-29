package ru.yaklimenko.fuel.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;

import java.io.File;
import java.util.List;

import ru.yaklimenko.fuel.utils.ReflectionsUtil;

/**
 * Created by Антон on 21.05.2016.
 */
public class DBHelper extends OrmLiteSqliteOpenHelper {
    
    public static final String TAG = DBHelper.class.getSimpleName();

    //имя файла базы данных который будет храниться в /data/data/APPNAME/DATABASE_NAME
    private static final String DATABASE_NAME ="fuelProject.sqlite";

    //с каждым увеличением версии, при нахождении в устройстве БД с предыдущей версией будет выполнен метод onUpgrade();
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(
                context,
                getPath(context) + "/database/" + DATABASE_NAME,
                null,
                DATABASE_VERSION
        );
    }

    private static File getPath(Context context) {
        String state = Environment.getExternalStorageState();
        File path;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            path = context.getApplicationContext().getExternalFilesDir(null);
        } else {
            path = context.getApplicationContext().getFilesDir();
        }
        return path;
    }

    public DBHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion) {
        super(context, databaseName, factory, databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        Log.d(TAG, "onCreate: ");
        List<Class> tableClasses;
        try {
            tableClasses = ReflectionsUtil.getClasses("ru.yaklimenko.fuel.db.entities");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        String zu = "";
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }
}
