package ru.yaklimenko.fuel.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.yaklimenko.fuel.R;
import ru.yaklimenko.fuel.db.dao.FillingStationDao;
import ru.yaklimenko.fuel.db.dao.FuelCategoryDao;
import ru.yaklimenko.fuel.db.dao.FuelDao;
import ru.yaklimenko.fuel.db.entities.FillingStation;
import ru.yaklimenko.fuel.db.entities.Fuel;
import ru.yaklimenko.fuel.db.entities.FuelCategory;

/**
 * Created by Антон on 07.06.2016.
 * list of fuels for single station
 */
public class StationFragment extends Fragment {

    public static final String TAG = StationFragment.class.getSimpleName();
    public static final String STATION_ID_KEY = "stationIdKey";
    private int fillingStationId = -1;

    public static void openStationFragment(int fillingStationId, FragmentManager fragmentManager) {
        StationFragment stationFragment = new StationFragment();
        Bundle args = new Bundle();
        args.putInt(StationFragment.STATION_ID_KEY, fillingStationId);
        stationFragment.setArguments(args);
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, stationFragment, StationFragment.TAG)
                .addToBackStack(MapsFragment.TAG)
                .commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readArgs();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        readSavedValues(savedInstanceState);
        if (fillingStationId == -1) {
            throw new IllegalStateException("cannot load fragment - unknown station");
        }
        View view = inflater.inflate(R.layout.fragment_station, container, false);
        fillFragmentWithData(view);
        return view;
    }

    private void fillFragmentWithData(View view) {
        FillingStation station = FillingStationDao.getInstance().queryForId(fillingStationId);
        if (station == null) {
            throw new IllegalStateException("cannot find needed station in db");
        }
        TextView stationTitleView = (TextView)view.findViewById(R.id.stationTitle);
        stationTitleView.setText(station.name);

        TextView stationAddressView = (TextView)view.findViewById(R.id.stationAddress);
        stationAddressView.setText(station.address);

        ListView fuelsListView = (ListView)view.findViewById(R.id.fuelsList);
        FuelAdapter adapter = prepareAdapter(station);
        fuelsListView.setAdapter(adapter);
    }

    @NonNull
    private FuelAdapter prepareAdapter(FillingStation station) {
        List<Fuel> fuelsList = FuelDao.getInstance().getByStation(station.id);
        List<Integer> fuelCategoryIdList = new ArrayList<>();
        for (Fuel f : fuelsList) {
            fuelCategoryIdList.add(f.categoryId);
        }
        List<FuelCategory> fuelCategories = FuelCategoryDao.getInstance()
                .getByIdList(fuelCategoryIdList);

        Map<Integer, FuelCategory> fuelCategoriesByIds = new LinkedHashMap<>();
        List<Fuel> sortedFuelList = new ArrayList<>();
        for (FuelCategory fCategory : fuelCategories) {
            fuelCategoriesByIds.put(fCategory.id, fCategory);
            Fuel tempFuel = null;
            for (Fuel f : fuelsList) {
                if (f.categoryId == fCategory.id) {
                    tempFuel = f;
                    break;
                }
            }
            sortedFuelList.add(tempFuel);
            fuelsList.remove(tempFuel);
        }
        return new FuelAdapter(sortedFuelList, fuelCategoriesByIds);
    }

    private void readArgs() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return;
        }

        if (arguments.containsKey(STATION_ID_KEY)) {
            fillingStationId = arguments.getInt(STATION_ID_KEY);
        }
    }

    private void readSavedValues(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        if (savedInstanceState.containsKey(STATION_ID_KEY)) {
            fillingStationId = savedInstanceState.getInt(STATION_ID_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATION_ID_KEY, fillingStationId);
    }

    private class FuelAdapter extends BaseAdapter{

        private List<Fuel> items;
        private Map<Integer, FuelCategory> fuelCategoriesById = new HashMap<>();

        public FuelAdapter(List<Fuel> items, Map<Integer, FuelCategory> fuelCategoriesById) {
            this.items = items;
            this.fuelCategoriesById = fuelCategoriesById;
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
                    LayoutInflater.from(getActivity()).inflate(R.layout.fuel_list_item, parent, false) :
                    convertView;
            Fuel fuel = items.get(position);
            TextView fuelCategoryView = (TextView) view.findViewById(R.id.fuelCategory);
            fuelCategoryView.setText(fuelCategoriesById.get(fuel.categoryId).name);

            TextView fuelPriceView = (TextView) view.findViewById(R.id.fuelPrice);
            fuelPriceView.setText(String
                    .format(getResources().getConfiguration().locale, "%.2f%n", fuel.price)
            );

            return view;
        }
    }

}
