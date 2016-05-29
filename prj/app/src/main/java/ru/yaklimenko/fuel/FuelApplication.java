package ru.yaklimenko.fuel;

import android.app.Application;

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

    }
}
