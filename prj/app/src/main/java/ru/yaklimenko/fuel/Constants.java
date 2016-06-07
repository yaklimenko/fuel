package ru.yaklimenko.fuel;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Антон on 27.05.2016.
 * Constants
 */
public class Constants {

    //locations
    public static final LatLng TOMSK_LATLNG = new LatLng(56.492d, 85d);

    //request codes
    public static final int MY_LOCATION_PERMISSIONS_REQUEST_CODE = 101;
    public static final int FUEL_FILTERED_REQUEST_CODE = 102;
    public static final int FUEL_FILTERED2_REQUEST_CODE = 103;
    public static final int PRICE_SORTED_REQUEST_CODE = 104;

    //network
    public static final String SERVER_URL = "http://109.120.189.182";
    public static final String PREFIX = "api/v1";


    //misc
    public static final long STATIONS_UPDATE_PERIOD = 4 * 60 * 60 * 1000; //4 hours
    public static final long STATIONS_UPDATE_CHECK_PERIOD = 1000; //4 minutes

}
