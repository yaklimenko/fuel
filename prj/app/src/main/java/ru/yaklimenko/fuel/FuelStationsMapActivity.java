package ru.yaklimenko.fuel;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import ru.yaklimenko.fuel.db.entities.FillingStation;
import ru.yaklimenko.fuel.net.DataLoader;

public class FuelStationsMapActivity extends Activity implements OnMapReadyCallback {
    public static final String TAG = FuelStationsMapActivity.class.getSimpleName();
    private GoogleMap mMap;
    private List<Marker> stationsMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_stations_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Add a marker in Sydney and move the camera
        LatLng tomsk = new LatLng(56.492d, 85d);
//        mMap.addMarker(new MarkerOptions().position(tomsk).title("Tomsk"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(tomsk));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(12f));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG, "onMapClick: " + latLng.toString());
            }
        });
        drawMyLocation();
        refreshFillingStations();
    }

    private void drawMyLocation() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            boolean fineLocationGranted = checkSelfPermission(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED;

            boolean coarseLocationGranted = checkSelfPermission(
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED;

            if (!fineLocationGranted && !coarseLocationGranted) {
                String[] permissions = new String[2];
                permissions[0] = android.Manifest.permission.ACCESS_FINE_LOCATION;
                permissions[1] = android.Manifest.permission.ACCESS_COARSE_LOCATION;
                requestPermissions(permissions, Constants.MY_LOCATION_PERMISSIONS_REQUEST_CODE);
            } else {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.MY_LOCATION_PERMISSIONS_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    drawMyLocation();
                    break;
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.maps_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == R.id.refreshStations){
            onRefreshStationsMenuItemClicked();
        } else if (item.getItemId() == R.id.filterFuel) {
            onFuelSelectMenuItemClicked();
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public void onRefreshStationsMenuItemClicked () {
        refreshFillingStations();
    }

    public void onFuelSelectMenuItemClicked() {
        String zu = "";
    }

    private void refreshFillingStations() {
        new DataLoader().getStations(this, new DataLoader.OnDataGotListener() {
            @Override
            public void onDataLoaded(List<FillingStation> fillingStations) {
                setStations(fillingStations);
            }
        });
    }

    private void setStations(List<FillingStation> stations) {
        Log.d(TAG, "setStations: trying to refresh stations on map");
        for (Marker marker : stationsMarkers) {
            marker.remove();
        }
        stationsMarkers.clear();
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.pin);
        for (FillingStation fillingStation : stations) {
            LatLng stationPosition = new LatLng(fillingStation.latitude, fillingStation.longitude);
            stationsMarkers.add(
                    mMap.addMarker(new MarkerOptions()
                            .position(stationPosition)
                            .title(fillingStation.name)
                            .icon(descriptor)
                    )
            );
        }

    }


}
