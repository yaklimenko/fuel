package ru.yaklimenko.fuel;

/**
 * Created by Антон on 27.05.2016.
 * Constants
 */
public class Constants {

    //request codes
    public static final int MY_LOCATION_PERMISSIONS_REQUEST_CODE = 101;

    //network
    public static final String SERVER_URL = "http://109.120.189.182";
    public static final String PREFIX = "api/v1";

    //settings
    public static final String STATIONS_lAST_UPDATE_KEY = "stationsLastLoadKey";

    //misc
    public static final long STATIONS_UPDATE_PERIOD = 4 * 60 * 60 * 1000; //4 hours

}
