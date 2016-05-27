package ru.yaklimenko.fuel;

import android.app.Application;

import java.sql.SQLException;

import ru.yaklimenko.fuel.db.DbHelperManager;
import ru.yaklimenko.fuel.db.entities.FillingStation;

/**
 * Created by Антон on 25.05.2016.
 */
public class FuelApplication extends Application {

    public static final String TAG = FuelApplication.class.getSimpleName();

    public static FuelApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        DbHelperManager.setHelper(getApplicationContext());
        try {
            DbHelperManager.getHelper().getDao(FillingStation.class).queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
