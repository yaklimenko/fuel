package ru.yaklimenko.fuel.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import ru.yaklimenko.fuel.Constants;
import ru.yaklimenko.fuel.FuelApplicationPreferences;
import ru.yaklimenko.fuel.R;
import ru.yaklimenko.fuel.db.dao.FillingStationDao;
import ru.yaklimenko.fuel.db.dao.FuelCategoryDao;
import ru.yaklimenko.fuel.db.entities.FillingStation;
import ru.yaklimenko.fuel.db.entities.FuelCategory;
import ru.yaklimenko.fuel.dialogs.ConnectionProblemDialogFragment;
import ru.yaklimenko.fuel.dialogs.FilterFuelDialogFragment;
import ru.yaklimenko.fuel.net.DataLoader;
import ru.yaklimenko.fuel.services.FillingStationsService;
import ru.yaklimenko.fuel.utils.CommonUtil;

/**
 * Created by Антон on 30.05.2016.
 * fragment with maps
 */
public class MapsFragment
        extends Fragment
        implements OnMapReadyCallback, DialogInterface.OnClickListener {

    public static final String TAG = MapsFragment.class.getSimpleName();
    private static final float DEFAULT_ZOOM = 12f;
    public static final String CAMERA_POSITION_KEY = "CameraPositionKey";
    public static final String STATIONS_KEY = "StationsKey";
    public static final String FUEL_CATEGORY_KEY = "FuelCategoryKey";
    public static final String STATION_TO_CENTER_KEY = "StationToCenterKey";

    private GoogleMap mMap;

    private List<Marker> stationsMarkers = new ArrayList<>();
    private List<FillingStation> stations;
    private Map<Marker, FillingStation> stationsByMarkers = new WeakHashMap<>();
    private Map<FillingStation, Marker> markersByStations = new WeakHashMap<>();


    boolean isMapReady, isFirstTimeCameraConfigured = false;
    CameraPosition usersCameraPosition;

    boolean isRefreshButtonVisible = false;
    boolean isFilterFuelButtonVisible = false;

    FuelCategory fuelCategory;

    private OnMapLoadedListener onMapLoadedListener;

    public static void openMapsFragment(FragmentManager fManager) {
        Fragment f = fManager.findFragmentByTag(TAG);
        if (f == null) {
            f = new MapsFragment();
        }
        fManager.beginTransaction()
                .replace(R.id.content_frame, f, TAG)
                .addToBackStack(TAG)
                .commit();
    }

    public static void openMapsFragmentForStation(FragmentManager fManager, int stationToCenterId) {

        Fragment f = fManager.findFragmentByTag(TAG);
        if (f == null) {
            f = new MapsFragment();
        }
        Bundle args = f.getArguments();
        args.putInt(STATION_TO_CENTER_KEY, stationToCenterId);
        fManager.beginTransaction()
                .replace(R.id.content_frame, f, TAG)
                .addToBackStack(TAG)
                .commit();
    }

    public static void openMapsFragmentForFuelCategory(FragmentManager fManager, int fuelCategoryId) {

        Fragment f = fManager.findFragmentByTag(TAG);
        if (f == null) {
            f = new MapsFragment();
        }
        Bundle args = f.getArguments();
        args.putInt(FUEL_CATEGORY_KEY, fuelCategoryId);
        fManager.beginTransaction()
                .replace(R.id.content_frame, f, TAG)
                .addToBackStack(TAG)
                .commit();
    }

    public MapsFragment() {
        super();
        // Just to be an empty Bundle. You can use this later with getArguments().set...
        setArguments(new Bundle());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_maps, container, false);
        MapFragment mapFragment = getMapFragment();
        mapFragment.getMapAsync(this);
        readSavedValues(savedInstanceState);
        readCategoryFromArgs();
        setHasOptionsMenu(true);
        setTitle();

        return root;
    }

    private void readCategoryFromArgs() {
        if (!getArguments().containsKey(FUEL_CATEGORY_KEY)) {
            return;
        }
        fuelCategory = FuelCategoryDao.getInstance().queryForId(getArguments().getInt(FUEL_CATEGORY_KEY));
        getArguments().remove(FUEL_CATEGORY_KEY);
    }

    private void setTitle() {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar == null) {
            Log.e(TAG, "setTitle: cannot find action bar");
            return;
        }
        actionBar.setTitle(R.string.app_mode_map);
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

        if (savedInstanceState.containsKey(FUEL_CATEGORY_KEY)) {
            fuelCategory = (FuelCategory) savedInstanceState.getSerializable(FUEL_CATEGORY_KEY);
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
        isMapReady = true;
        if (onMapLoadedListener != null) {
            onMapLoadedListener.onMapLoaded();
        }
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (new FuelApplicationPreferences(getActivity()).isNeedToCheckStations())  {
            refreshFillingStations(false);
        }
        configCamera();
    }

    private void configCamera() {
        Marker marketToCenter = getMarkerFromArgs();
        if (marketToCenter != null) {
            moveCameraToMarker(marketToCenter);
            return;
        }

        if (usersCameraPosition != null) {
            moveToSavedPosition();
            return;
        }
        moveToMyOrCityCenterLocation();
    }

    @Nullable
    private Marker getMarkerFromArgs() {
        Bundle args = getArguments();
        int stationToCenterId = args.getInt(STATION_TO_CENTER_KEY, -1);
        args.remove(STATION_TO_CENTER_KEY);
        if (stationToCenterId == -1) {
            return null;
        }

        FillingStation stationToCenter = FillingStationDao.getInstance()
                .queryForId(stationToCenterId);
        Marker marketToCenter = markersByStations.get(stationToCenter);
        if (marketToCenter == null) {
            throw new IllegalStateException("cannot find marker");
        }
        return marketToCenter;
    }

    private void moveCameraToMarker(Marker markerToCenter) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                markerToCenter.getPosition(), DEFAULT_ZOOM + 2
        ));
        markerToCenter.showInfoWindow();
    }

    private void moveToSavedPosition() {
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(usersCameraPosition));
        usersCameraPosition = null;
    }

    private void moveToMyOrCityCenterLocation() {
        //LatLng user = new LatLng(location.getLatitude(), location.getLongitude());
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

        Location usersLocation = null;
        LocationManager locationManager = (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getAllProviders();
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            usersLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            providers.remove(LocationManager.GPS_PROVIDER);
        }
        if (usersLocation == null && providers.contains(LocationManager.NETWORK_PROVIDER)) {
            usersLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            providers.remove(LocationManager.NETWORK_PROVIDER);
        }
        if (usersLocation == null) {
            for (String provider : providers) {
                usersLocation = locationManager.getLastKnownLocation(provider);
                if (usersLocation != null) {
                    break;
                }
            }
        }

        if (usersLocation != null) {
            mMap.setMyLocationEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(usersLocation.getLatitude(), usersLocation.getLongitude())));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        } else {
            moveToTomsk();
        }
    }

    private void moveToTomsk() {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Constants.TOMSK_LATLNG));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.maps_fragment_menu, menu);
        MenuItem refreshMenuItem = menu.findItem(R.id.refreshStations);
        refreshMenuItem.setVisible(isRefreshButtonVisible);
        MenuItem fuelTypeMenuItem = menu.findItem(R.id.filterFuel);
        fuelTypeMenuItem.setVisible(isFilterFuelButtonVisible);
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

    private void setRefreshButtonVisibility(boolean isVisible) {
        isRefreshButtonVisible = isVisible;
        getActivity().invalidateOptionsMenu();
    }

    private void setFilterFuelButtonVisibility(boolean isVisible) {
        isFilterFuelButtonVisible = isVisible;
        getActivity().invalidateOptionsMenu();
    }



    public void onRefreshStationsMenuItemClicked () {
        refreshFillingStations(true);
    }

    public void onFuelSelectMenuItemClicked() {
        FilterFuelDialogFragment filterFuelDialog = FilterFuelDialogFragment
                .getInstance(fuelCategory == null ? null : fuelCategory.id);
        filterFuelDialog.setTargetFragment(this, Constants.FUEL_FILTERED_REQUEST_CODE);
        filterFuelDialog.show(getFragmentManager(), FilterFuelDialogFragment.TAG);
    }

    private void refreshFillingStations(boolean forceUpdate) {
        if (!forceUpdate && stations != null && !stations.isEmpty()) {
            setStations(stations);
        }
        new DataLoader().getStations(getActivity(), new DataLoader.OnDataGotListener() {
            @Override
            public void onDataLoaded(List<FillingStation> fillingStations) {
                setStations(fillingStations);
            }

            @Override
            public void onError(Throwable e) {
                if (e instanceof ConnectException) {
                    showConnectionProblemDialog();
                } else {
                    showProblemDialog();
                }
            }
        });
        setRefreshButtonVisibility(false);
    }

    private void showProblemDialog() {
        ConnectionProblemDialogFragment.getInstance(R.string.dialog_text_server_unreachable)
                .show(getFragmentManager(), ConnectionProblemDialogFragment.TAG);
        setRefreshButtonVisibility(true);
    }

    private void showConnectionProblemDialog() {
        ConnectionProblemDialogFragment.getInstance(R.string.dialog_text_connection_problem)
                .show(getFragmentManager(), ConnectionProblemDialogFragment.TAG);
        setRefreshButtonVisibility(true);
    }

    private void setStations(List<FillingStation> stations) {
        this.stations = stations;
        Log.d(TAG, "setStations: trying to refresh stations on map");
        for (Marker marker : stationsMarkers) {
            marker.remove();
        }
        stationsMarkers.clear();
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.pin);

        List<FillingStation> filteredStations;
        if (getArguments().containsKey(STATION_TO_CENTER_KEY)) {
            fuelCategory = null;
        }
        if (fuelCategory != null) {
            filteredStations = FillingStationsService.filterStationsByFuelCategory(
                    stations, fuelCategory
            );
        } else {
            filteredStations = stations;
        }

        for (FillingStation fillingStation : filteredStations) {

            LatLng stationPosition = new LatLng(fillingStation.latitude, fillingStation.longitude);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(stationPosition)
                    .title(fillingStation.name)
                    .snippet(fillingStation.address)
                    .icon(descriptor)

            );
            stationsMarkers.add(marker);
            stationsByMarkers.put(marker, fillingStation);
            markersByStations.put(fillingStation, marker);
        }
        setFilterFuelButtonVisibility(true);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                FillingStation clickedStation = stationsByMarkers.get(marker);
                StationFragment.openStationFragment(
                        clickedStation.id, getActivity().getFragmentManager()
                );
            }
        });
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

        if (fuelCategory != null) {
            outState.putSerializable(FUEL_CATEGORY_KEY, fuelCategory);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case Constants.FUEL_FILTERED_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                     int fuelCategoryId =
                             data.getIntExtra(FilterFuelDialogFragment.FUEL_CATEGORY_KEY, -1);
                    onFuelCategoryChanged(fuelCategoryId);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onFuelCategoryChanged(int fuelCategoryId) {
        FuelCategory newFuelCategory;
        if (fuelCategoryId == -1) {
            newFuelCategory = null;
        } else {
            newFuelCategory = FuelCategoryDao.getInstance().queryForId(fuelCategoryId);
        }
        if (!CommonUtil.equals(newFuelCategory, fuelCategory)) {
            fuelCategory = newFuelCategory;
            setStations(stations);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FragmentManager fm;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            fm = getFragmentManager();
        } else {
            fm = getChildFragmentManager();
        }
        MapFragment f = (MapFragment) fm.findFragmentById(R.id.map);
        if (f != null) {
            fm.beginTransaction().remove(f).commitAllowingStateLoss();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }

    public void setOnMapLoadedListener(OnMapLoadedListener onMapLoadedListener) {
        this.onMapLoadedListener = onMapLoadedListener;
    }

    public interface OnMapLoadedListener {
        void onMapLoaded();
    }
}
