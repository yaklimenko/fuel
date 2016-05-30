package ru.yaklimenko.fuel.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ru.yaklimenko.fuel.Constants;
import ru.yaklimenko.fuel.FuelStationsMapActivity;
import ru.yaklimenko.fuel.R;
import ru.yaklimenko.fuel.db.entities.FillingStation;
import ru.yaklimenko.fuel.net.DataLoader;

/**
 * Created by Антон on 30.05.2016.
 * fragment with maps
 */
public class MapsFragment
        extends Fragment
        implements OnMapReadyCallback, FuelStationsMapActivity.OnLocationGotListener {

    public static final String TAG = MapsFragment.class.getSimpleName();
    public static final String CAMERA_POSITION_KEY =
            MapsFragment.class.getCanonicalName() + ".CameraPositionKey";
    public static final String STATIONS_KEY =
            MapsFragment.class.getCanonicalName() + ".StationsKey";

    private GoogleMap mMap;

    private List<Marker> stationsMarkers = new ArrayList<>();
    private List<FillingStation> stations;

    boolean isMapReady, isLocationGot, isFirstTimeCameraConfigured = false;
    Location usersLocation;
    CameraPosition usersCameraPosition;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_maps, container);
        //setRetainInstance(true);
        MapFragment mapFragment = getMapFragment();
        mapFragment.getMapAsync(this);

        readSavedValues(savedInstanceState);
        if (!isFirstTimeCameraConfigured) {
            ((FuelStationsMapActivity) getActivity()).setOnLocationGotListener(this);
        }
        return root;
    }

    private void readSavedValues(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }

        if (savedInstanceState.containsKey(STATIONS_KEY)) {
            Type stationsListType = new TypeToken<List<FillingStation>>(){}.getType();
            Gson gson = new Gson();
            stations = gson.fromJson(savedInstanceState.getString(STATIONS_KEY), stationsListType);
        }

        if (savedInstanceState.containsKey(CAMERA_POSITION_KEY)) {
            isFirstTimeCameraConfigured = true;
            usersCameraPosition = savedInstanceState.getParcelable(CAMERA_POSITION_KEY);
        }
    }

    private MapFragment getMapFragment() {
        FragmentManager fm;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            fm = getFragmentManager();
        } else {
            fm = getChildFragmentManager();
        }
        return (MapFragment) fm.findFragmentById(R.id.map);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");
        isMapReady = true;
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        configCamera();


        refreshFillingStations();
    }

    private void configCamera() {

        if (usersCameraPosition != null) {
            moveToSavedPosition();
            return;
        }
        if (isLocationGot) {
            ((FuelStationsMapActivity)getActivity()).onDataLoaded();
            if (usersLocation != null) {
                moveToMyLocation(usersLocation);
            } else {
                moveToTomsk();
            }
        } else {
            moveToTomsk();
        }

    }

    private void moveToSavedPosition() {
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(usersCameraPosition));
        usersCameraPosition = null;
        return;
    }

    private void moveToMyLocation(Location location) {
        LatLng user = new LatLng(location.getLatitude(), location.getLongitude());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            boolean fineLocationGranted = getContext().checkSelfPermission(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED;

            boolean coarseLocationGranted = getContext().checkSelfPermission(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED;

            if (!fineLocationGranted && !coarseLocationGranted) {
                return;//permissions were requested on host activity
            }
        }
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(user));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(12f));
    }

    private void moveToTomsk() {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Constants.TOMSK_LATLNG));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(12f));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.maps_activity_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refreshStations){
            onRefreshStationsMenuItemClicked();
            return true;
        }
        if (item.getItemId() == R.id.filterFuel) {
            onFuelSelectMenuItemClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }


    public void onRefreshStationsMenuItemClicked () {
        refreshFillingStations();
    }

    public void onFuelSelectMenuItemClicked() {
        String zu = "";
    }

    private void refreshFillingStations() {
        if (stations != null && !stations.isEmpty()) {
            setStations(stations);
        }
        new DataLoader().getStations(getActivity(), new DataLoader.OnDataGotListener() {
            @Override
            public void onDataLoaded(List<FillingStation> fillingStations) {
                setStations(fillingStations);
            }
        });
    }

    private void setStations(List<FillingStation> stations) {
        this.stations = stations;
        Log.d(TAG, "setStations: trying to refresh stations on map");
        for (Marker marker : stationsMarkers) {
            marker.remove();
        }
        stationsMarkers.clear();
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.pin);
        for (FillingStation fillingStation : stations) {
            LatLng stationPosition = new LatLng(fillingStation.latitude, fillingStation.longitude);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(stationPosition)
                    .title(fillingStation.name)
                    .snippet(fillingStation.address)
                    .icon(descriptor)

            );
            stationsMarkers.add(marker);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMap != null) {
            usersCameraPosition = mMap.getCameraPosition();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (stations != null && !stations.isEmpty()) {
            Gson gson = new Gson();
            Type stationsListType = new TypeToken<List<FillingStation>>(){}.getType();
            outState.putSerializable(STATIONS_KEY, gson.toJson(stations, stationsListType));
        }
        if (mMap != null) {
            CameraPosition position = mMap.getCameraPosition();
            outState.putParcelable(CAMERA_POSITION_KEY, position);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLocationGot(Location location) {
        if (isFirstTimeCameraConfigured) {
            return;
        }
        isLocationGot = true;
        usersLocation = location;
        if (usersCameraPosition != null) {
            moveToSavedPosition();
            return;
        }
        if (isMapReady) {
            if (location == null) {
                moveToTomsk();
            } else {
                moveToMyLocation(usersLocation);
            }
            ((FuelStationsMapActivity)getActivity()).onDataLoaded();
        }

    }
}
