package ru.yaklimenko.fuel;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import ru.yaklimenko.fuel.db.entities.FuelCategory;
import ru.yaklimenko.fuel.fragments.ListFragment;
import ru.yaklimenko.fuel.fragments.MapsFragment;

public class FuelStationsMapActivity extends Activity implements MapsFragment.OnMapLoadedListener {

    public static final String TAG = "ActivityTestTag";
    public static final String WAS_LOADED =
            FuelStationsMapActivity.class.getCanonicalName() + ".OnAlreadyBeenLoaded";

    private boolean wasLoaded;
    private OnFuelFilteredListener onFuelFilteredListener;

    private View loadingPanel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_stations_map);
        loadingPanel = findViewById(R.id.loadingPanel);
        readSavedValues(savedInstanceState);

        String[] modes = getResources().getStringArray(R.array.app_modes);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        drawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, modes));
        // Set the list's click listener
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                android.app.FragmentManager fManager = getFragmentManager();
                if (position == 0) {
                    fManager.beginTransaction()
                            .replace(R.id.content_frame, new MapsFragment(), MapsFragment.TAG)
                            .commit();

                } else if (position == 1) {
                    fManager.beginTransaction()
                            .replace(R.id.content_frame, new ListFragment(), ListFragment.TAG)
                            .addToBackStack(ListFragment.TAG)
                            .commit();
                }

            }
        });
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
        MapsFragment mapsFragment =
                (MapsFragment)getFragmentManager().findFragmentById(R.id.mapsFragment);

        if (mapsFragment != null) {
            mapsFragment.setOnMapLoadedListener(this);
        } else {
            loadingPanel.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart: ");
        super.onRestart();
    }

    private void readSavedValues(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        wasLoaded = savedInstanceState.getBoolean(WAS_LOADED);
        if (wasLoaded) {
            loadingPanel.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(WAS_LOADED, wasLoaded);
        super.onSaveInstanceState(outState);
    }


    public void onFuelFiltered(@Nullable FuelCategory fuelCategory) {
        if (onFuelFilteredListener != null) {
            onFuelFilteredListener.onFuelFiltered(fuelCategory);
        }
    }

    public void setOnFuelFilteredListener(OnFuelFilteredListener onFuelFilteredListener) {
        this.onFuelFilteredListener = onFuelFilteredListener;
    }

    @Override
    public void onMapLoaded() {
        loadingPanel.setVisibility(View.GONE);
        wasLoaded = true;
    }


    public interface OnFuelFilteredListener{
        void onFuelFiltered(FuelCategory fuelCategory);
    }




}
