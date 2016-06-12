package ru.yaklimenko.fuel;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import ru.yaklimenko.fuel.fragments.MapsFragment;
import ru.yaklimenko.fuel.fragments.StationsByFuelFragment;

public class FuelStationsMapActivity extends AppCompatActivity implements MapsFragment.OnMapLoadedListener {

    public static final String TAG = "ActivityTestTag";
    public static final String WAS_LOADED_KEY = "wasLoadedKeyKey";
    public static final String CURRENT_FRAGMENT_TAG_KEY = "currentFragmentTagKey";

    private boolean wasLoaded;

    private View loadingPanel;

    DrawerLayout drawerLayout;

    private String currentFragmentTag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_stations_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        loadingPanel = findViewById(R.id.loadingPanel);
        readSavedValues(savedInstanceState);

        String[] modes = getResources().getStringArray(R.array.app_modes);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView drawerList = (ListView) findViewById(R.id.left_drawer);

        drawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, modes));

        if (currentFragmentTag == null) {
            currentFragmentTag = MapsFragment.TAG;
            MapsFragment.openMapsFragment(getFragmentManager());
        }

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                android.app.FragmentManager fManager = getFragmentManager();
                if (position == 0) {
                    currentFragmentTag = MapsFragment.TAG;
                    MapsFragment.openMapsFragment(fManager);
                } else if (position == 1) {
                    currentFragmentTag = StationsByFuelFragment.TAG;
                    StationsByFuelFragment.openStationsByFuelsFragment(fManager);
                }
                drawerLayout.closeDrawer(drawerList);
            }
        });

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.app_menu, R.string.app_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    @Override
    protected void onStart() {
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
}
