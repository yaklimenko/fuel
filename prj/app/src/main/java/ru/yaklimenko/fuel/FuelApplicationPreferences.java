package ru.yaklimenko.fuel;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Date;

/**
 * Created by Антон on 31.05.2016.
 * helper for shared preferences
 */
public class FuelApplicationPreferences {

    public static final String STATIONS_lAST_UPDATE_KEY = "stationsLastLoadKey";
    public static final String STATIONS_lAST_CHECK_KEY = "stationsLastCheckKey";

    Context context;
    SharedPreferences settings;


    private FuelApplicationPreferences() {/*empty*/}

    public FuelApplicationPreferences(Context context) {
        this.context = context;
        settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void updateStationsLastUpdateTime() {
        saveStationsLastUpdateTime(new Date());
    }

    public void saveStationsLastUpdateTime(Date date) {
        settings.edit()
                .putLong(STATIONS_lAST_UPDATE_KEY, date.getTime())
                .apply();
    }

    public boolean areStationsActual() {
        return new Date().getTime() - settings.getLong(STATIONS_lAST_UPDATE_KEY, 0L) <
                Constants.STATIONS_UPDATE_PERIOD;
    }

    public void updateStationsLastCheckTime() {
        saveStationsLastCheckTime(new Date());
    }

    public void saveStationsLastCheckTime(Date date) {
        settings.edit()
                .putLong(STATIONS_lAST_CHECK_KEY, date.getTime())
                .apply();
    }

    public boolean isNeedToCheckStations() {
        return new Date().getTime() - settings.getLong(STATIONS_lAST_CHECK_KEY, 0L) >
                Constants.STATIONS_UPDATE_CHECK_PERIOD;
    }

}
