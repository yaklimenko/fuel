package ru.yaklimenko.fuel;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import ru.yaklimenko.fuel.db.entities.FuelCategory;

public class FuelStationsMapActivity
        extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = FuelStationsMapActivity.class.getSimpleName();
    public static final String WAS_LOADED =
            FuelStationsMapActivity.class.getCanonicalName() + ".OnAlreadyBeenLoaded";

    private boolean wasLoaded;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private OnLocationGotListener onLocationGotListener;
    private OnFuelFilteredListener onFuelFilteredListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_stations_map);
        readSavedValues(savedInstanceState);
        if (wasLoaded) {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
        tryGetUsersLocation();
    }

    private void readSavedValues(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        wasLoaded = savedInstanceState.getBoolean(WAS_LOADED);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(WAS_LOADED, wasLoaded);
        super.onSaveInstanceState(outState);
    }


    private void tryGetUsersLocation() {
        if (onLocationGotListener == null) {
            return;
        }
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        getLastLocation();
    }

    private void getLastLocation() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            boolean fineLocationGranted = checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED;

            boolean coarseLocationGranted = checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED;

            if (!fineLocationGranted && !coarseLocationGranted) {
                String[] permissions = new String[2];
                permissions[0] = Manifest.permission.ACCESS_FINE_LOCATION;
                permissions[1] = Manifest.permission.ACCESS_COARSE_LOCATION;
                requestPermissions(permissions, Constants.MY_LOCATION_PERMISSIONS_REQUEST_CODE);
            } else {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                if (onLocationGotListener != null) {
                    onLocationGotListener.onLocationGot(mLastLocation);
                }
            }
        }


        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (onLocationGotListener != null) {
            onLocationGotListener.onLocationGot(mLastLocation);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults
    ) {
        if (requestCode == Constants.MY_LOCATION_PERMISSIONS_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();
                    break;
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //todo show some sad dialog
    }

    public void onDataLoaded () {
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        wasLoaded = true;
    }

    public void onFuelFiltered(@Nullable FuelCategory fuelCategory) {
        if (onFuelFilteredListener != null) {
            onFuelFilteredListener.onFuelFiltered(fuelCategory);
        }
    }

    public void setOnLocationGotListener(OnLocationGotListener onLocationGotListener) {
        this.onLocationGotListener = onLocationGotListener;
    }

    public void setOnFuelFilteredListener(OnFuelFilteredListener onFuelFilteredListener) {
        this.onFuelFilteredListener = onFuelFilteredListener;
    }


    public interface OnFuelFilteredListener{
        void onFuelFiltered(FuelCategory fuelCategory);
    }

    public interface OnLocationGotListener {
        void onLocationGot(Location location);
    }
}
