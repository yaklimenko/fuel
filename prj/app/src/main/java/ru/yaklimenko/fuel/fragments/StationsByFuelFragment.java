package ru.yaklimenko.fuel.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.yaklimenko.fuel.Constants;
import ru.yaklimenko.fuel.R;
import ru.yaklimenko.fuel.db.dao.FillingStationDao;
import ru.yaklimenko.fuel.db.dao.FuelCategoryDao;
import ru.yaklimenko.fuel.db.dao.FuelDao;
import ru.yaklimenko.fuel.db.entities.FillingStation;
import ru.yaklimenko.fuel.db.entities.Fuel;
import ru.yaklimenko.fuel.db.entities.FuelCategory;
import ru.yaklimenko.fuel.dialogs.FilterFuelDialogFragment;
import ru.yaklimenko.fuel.dialogs.SortByPriceDialogFragment;
import ru.yaklimenko.fuel.utils.CommonUtil;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Антон on 02.06.2016.
 * list fragment
 */
public class StationsByFuelFragment extends Fragment {

    public static final String TAG = StationsByFuelFragment.class.getSimpleName();
    public static final String CATEGORY_KEY = "FuelCategoryKey";
    public static final String ASCENDING_SORTING_KEY = "ascendingSortingKey";
    public static final String DEFAULT_CATEGORY_NAME = "АИ-95";

    private ListView stationsByFuelList;
    private FuelCategory selectedCategory;
    private Map<FillingStation, Fuel> fuelsByStations;
    private boolean priceAscending = true;

    public static void openStationsByFuelsFragment(FragmentManager fManager) {
        Fragment f = fManager.findFragmentByTag(TAG);
        if (f == null) {
            f = new StationsByFuelFragment();
        }
        fManager.beginTransaction()
                .replace(R.id.content_frame, f, TAG)
                .addToBackStack(TAG)
                .commit();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        readArgs();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_stations_by_fuel, container, false);
        stationsByFuelList = (ListView)fragmentView.findViewById(R.id.stationsByFuelList);
        readSavedValues(savedInstanceState);
        setTitle();
        fillList();
        return fragmentView;
    }

    private void setTitle() {
        if (selectedCategory == null) {
            Log.e(TAG, "setTitle: there is no fuel category to set title");
        }
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar == null) {
            Log.e(TAG, "setTitle: cannot find action bar");
            return;
        }
        actionBar.setTitle(selectedCategory.name);
    }

    private void readSavedValues(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        if (savedInstanceState.containsKey(CATEGORY_KEY)) {
            selectedCategory = FuelCategoryDao.getInstance()
                    .queryForId(savedInstanceState.getInt(CATEGORY_KEY));
        }
        priceAscending = savedInstanceState.getBoolean(ASCENDING_SORTING_KEY, true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.stations_by_fuel_fragment_menu, menu);
        MenuItem fuelTypeMenuItem = menu.findItem(R.id.filterFuel);
        fuelTypeMenuItem.setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.filterFuel) {
            onFuelSelectMenuItemClicked();
            return true;
        } else if (item.getItemId() == R.id.sortByPrice) {
            onSortByPriceMenuClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private void onFuelSelectMenuItemClicked() {
        FilterFuelDialogFragment filterFuelDialog = FilterFuelDialogFragment
                .getInstance(selectedCategory == null ? null : selectedCategory.id, false);
        filterFuelDialog.setTargetFragment(this, Constants.FUEL_FILTERED2_REQUEST_CODE);
        filterFuelDialog.show(getFragmentManager(), FilterFuelDialogFragment.TAG);
    }

    private void onSortByPriceMenuClicked() {
        SortByPriceDialogFragment sortByPriceDialogFragment = SortByPriceDialogFragment
                .getInstance(priceAscending);
        sortByPriceDialogFragment.setTargetFragment(this, Constants.PRICE_SORTED_REQUEST_CODE);
        sortByPriceDialogFragment.show(getFragmentManager(), SortByPriceDialogFragment.TAG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case Constants.FUEL_FILTERED2_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    int fuelCategoryId =
                            data.getIntExtra(FilterFuelDialogFragment.FUEL_CATEGORY_KEY, -1);
                    FuelCategory newFuelCategory;
                    if (fuelCategoryId == -1) {
                        newFuelCategory = null;
                    } else {
                        newFuelCategory = FuelCategoryDao.getInstance().queryForId(fuelCategoryId);
                    }
                    if (!CommonUtil.equals(newFuelCategory, selectedCategory)) {
                        selectedCategory = newFuelCategory;
                        fillList();
                    }
                }
                break;
            case Constants.PRICE_SORTED_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    priceAscending = data
                            .getBooleanExtra(SortByPriceDialogFragment.ASCENDING_SORTING_KEY, true);
                    fillList();
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void readArgs() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            setDefaultOrFirstCategory();
        } else {
            int catId = arguments.getInt(CATEGORY_KEY, -1);
            if (catId == -1) {
                setDefaultOrFirstCategory();
            } else {
                selectedCategory = FuelCategoryDao.getInstance().queryForId(catId);
            }
        }
    }

    private void setDefaultOrFirstCategory() {
        FuelCategory category = FuelCategoryDao.getInstance().getByName(DEFAULT_CATEGORY_NAME);
        if (category == null) {
            category = FuelCategoryDao.getInstance().getFirst();
        }
        selectedCategory = category;
    }

    private void fillList() {
        final Date stopwatch = new Date();
        Action1<Map<FillingStation, Fuel>> onNextAction = new Action1<Map<FillingStation, Fuel>>() {
            @Override
            public void call(Map<FillingStation, Fuel> fillingStationFuelMap) {
                fuelsByStations = fillingStationFuelMap;
                List<FillingStation> stations = new ArrayList<>(fillingStationFuelMap.keySet());
                stationsByFuelList.setAdapter(new StationsByFuelAdapter(stations));
                Log.d(TAG, "fillList: overall:" + (new Date().getTime() - stopwatch.getTime()));
            }
        };

        Observable.create( new Observable.OnSubscribe<Map<FillingStation, Fuel>>() {
            @Override
            public void call(Subscriber<? super Map<FillingStation, Fuel>> subscriber) {
                subscriber.onNext(getFuelsByStationsMap());
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNextAction);
    }

    @NonNull
    private Map<FillingStation, Fuel> getFuelsByStationsMap() {
        Date stopwatch = new Date();
        List<Fuel> fuels = FuelDao.getInstance().getByCategory(selectedCategory.id, priceAscending);
        Log.d(TAG, "fillList: loaded fuels from db in:" + (new Date().getTime() - stopwatch.getTime()));

        Map<FillingStation, Fuel>  tempFuelsByStations = new LinkedHashMap<>();
        for (Fuel fuel : fuels) {
            FillingStation station = FillingStationDao.getInstance().queryForId(fuel.stationId);
            tempFuelsByStations.put(station, fuel);
        }
        Log.d(TAG, "fillList: loaded related stations from db in:" + (new Date().getTime() - stopwatch.getTime()));
        return tempFuelsByStations;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (selectedCategory != null) {
            outState.putInt(CATEGORY_KEY, selectedCategory.id);
        }
        outState.putBoolean(ASCENDING_SORTING_KEY, priceAscending);
        super.onSaveInstanceState(outState);
    }

    private class StationsByFuelAdapter extends BaseAdapter {

        private List<FillingStation> items;

        public StationsByFuelAdapter(List<FillingStation> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView == null ?
                    LayoutInflater.from(getActivity())
                            .inflate(R.layout.station_by_fuel_list_item, parent, false) :
                    convertView;
            Fuel fuel = fuelsByStations.get(items.get(position));
            final FillingStation station = items.get(position);
//            TextView category = (TextView)view.findViewById(R.id.stationsByFuelListCategory);
//            category.setText(selectedCategory.name);

            TextView price = (TextView)view.findViewById(R.id.stationsByFuelListPrice);
            price.setText(getActivity().getResources().getString(R.string.price, fuel.price));

            TextView name = (TextView)view.findViewById(R.id.stationsByFuelListStationName);
            name.setText(station.name);

            TextView address = (TextView)view.findViewById(R.id.stationsByFuelListStationAddress);
            address.setText(station.address);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StationFragment.openStationFragment(station.id, getActivity().getFragmentManager());
                }
            });

            return view;
        }

    }


}
