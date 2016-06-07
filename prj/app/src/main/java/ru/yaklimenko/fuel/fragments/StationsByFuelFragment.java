package ru.yaklimenko.fuel.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
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
 * Created by Антон on 02.06.2016.
 * list fragment
 */
public class StationsByFuelFragment extends Fragment {

    public static final String TAG = StationsByFuelFragment.class.getSimpleName();
    public static final String CTAEGORY_KEY =
            StationsByFuelFragment.class.getSimpleName() + "FuelCategoryKey";
    public static final String DEFAULT_CATEGORY_NAME = "АИ-95";

    private ListView stationsByFuelList;
    private FuelCategory selectedCategory;
    private Map<FillingStation, Fuel> fuelsByStations;
    private boolean isSortedByPriceAsc = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        readArgs();
        cacheData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_stations_by_fuel, container, false);
        stationsByFuelList = (ListView)fragmentView.findViewById(R.id.stationsByFuelList);
        fillList();
        return fragmentView;
    }

    private void readArgs() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            setDefaultOrFirstCategory();
        } else {
            int catId = arguments.getInt(CTAEGORY_KEY, -1);
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

    private void cacheData() {
        List<Fuel> fuels = FuelDao.getInstance().getByCategory(selectedCategory.id);
        fuelsByStations = new HashMap<>();
        for (Fuel fuel : fuels) {
            FillingStation station = FillingStationDao.getInstance().queryForId(fuel.stationId);
            fuelsByStations.put(station, fuel);
        }
    }

    private void fillList() {
        List<FillingStation> stations = new ArrayList<>(fuelsByStations.keySet());
        stationsByFuelList.setAdapter(new StationsByFuelAdapter(stations));
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
            FillingStation station = items.get(position);
            TextView category = (TextView)view.findViewById(R.id.stationsByFuelListCategory);
            category.setText(selectedCategory.name);

            TextView price = (TextView)view.findViewById(R.id.stationsByFuelListPrice);
            price.setText(getActivity().getResources().getString(R.string.price, fuel.price));

            TextView name = (TextView)view.findViewById(R.id.stationsByFuelListStationName);
            name.setText(station.name);

            TextView address = (TextView)view.findViewById(R.id.stationsByFuelListStationAddress);
            address.setText(station.address);

            return view;
        }
    }
}
