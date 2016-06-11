package ru.yaklimenko.fuel;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import ru.yaklimenko.fuel.fragments.StationsByFuelFragment;
import ru.yaklimenko.fuel.fragments.MapsFragment;

public class FuelStationsMapActivity extends Activity implements MapsFragment.OnMapLoadedListener {

    public static final String TAG = "ActivityTestTag";
    public static final String WAS_LOADED_KEY = "wasLoadedKeyKey";
    public static final String CURRENT_FRAGMENT_TAG_KEY = "currentFragmentTagKey";

    private boolean wasLoaded;

    private View loadingPanel;

    DrawerLayout drawerLayout;

    private String currentFragmentTag;

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_stations_map);
        loadingPanel = findViewById(R.id.loadingPanel);
        readSavedValues(savedInstanceState);

        String[] modes = getResources().getStringArray(R.array.app_modes);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView drawerList = (ListView) findViewById(R.id.left_drawer);

        drawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, modes));

        if (currentFragmentTag == null) {
            openMapsFragment(getFragmentManager());
        }

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                android.app.FragmentManager fManager = getFragmentManager();
                if (position == 0) {
                    openMapsFragment(fManager);
                } else if (position == 1) {
                    openStationsByFulesFragment(fManager);
                }
                drawerLayout.closeDrawer(drawerList);
            }
        });
    }

    private void openStationsByFulesFragment(FragmentManager fManager) {
        currentFragmentTag = StationsByFuelFragment.TAG;
        currentFragment = new StationsByFuelFragment();
        fManager.beginTransaction()
                .replace(R.id.content_frame, currentFragment, StationsByFuelFragment.TAG)
                .addToBackStack(StationsByFuelFragment.TAG)
                .commit();
    }

    private void openMapsFragment(FragmentManager fManager) {
        currentFragmentTag = MapsFragment.TAG;
        currentFragment = new MapsFragment();
        fManager.beginTransaction()
                .replace(R.id.content_frame, currentFragment, MapsFragment.TAG)
                .addToBackStack(MapsFragment.TAG)
                .commit();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
        MapsFragment mapsFragment =
                (MapsFragment)getFragmentManager().findFragmentByTag(MapsFragment.TAG);

        if (mapsFragment != null) {
            mapsFragment.setOnMapLoadedListener(this);
        } else {
            loadingPanel.setVisibility(View.GONE);
        }
    }

    private void readSavedValues(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG_KEY, null);
        wasLoaded = savedInstanceState.getBoolean(WAS_LOADED_KEY);
        if (wasLoaded) {
            loadingPanel.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(WAS_LOADED_KEY, wasLoaded);
        outState.putString(CURRENT_FRAGMENT_TAG_KEY, currentFragmentTag);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onMapLoaded() {
        loadingPanel.setVisibility(View.GONE);
        wasLoaded = true;
    }
//
//    @Override
//    public void onBackPressed() {
//
//        int count = getFragmentManager().getBackStackEntryCount();
//
//        if (count == 0) {
//            super.onBackPressed();
//            //additional code
//        } else {
//            getFragmentManager().popBackStack();
//        }
//
//    }


    @Override
    public void onBackPressed() {

        // If the fragment exists and has some back-stack entry
        if (currentFragment != null && currentFragment.getChildFragmentManager().getBackStackEntryCount() > 0){
            // Get the fragment fragment manager - and pop the backstack
            currentFragment.getChildFragmentManager().popBackStack();
        }
        // Else, nothing in the direct fragment back stack
        else{
            // Let super handle the back press
            super.onBackPressed();
        }
    }

}
