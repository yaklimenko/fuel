package ru.yaklimenko.fuel.services;

import java.util.ArrayList;
import java.util.List;

import ru.yaklimenko.fuel.db.dao.FuelDao;
import ru.yaklimenko.fuel.db.entities.*;

/**
 * Created by Антон on 31.05.2016.
 * soutines for stations
 */
public class FillingStationsService {

    public static List<FillingStation> filterStationsByFuelCategory (
            List<FillingStation> stations, FuelCategory fuelCategory
    ) {
        if (fuelCategory == null) {
            return stations;
        }
        List<FillingStation> resList = new ArrayList<>();
        List<Fuel> availableFuels = FuelDao.getInstance().getByCategory(fuelCategory.id);
        if (availableFuels == null || availableFuels.isEmpty()) {
            return resList;
        }

        List<Integer> stationIds = new ArrayList<>();
        for (Fuel fuel : availableFuels) {
            stationIds.add(fuel.stationId);
        }
        for (FillingStation station : stations) {
            if (stationIds.contains(station.id)) {
                resList.add(station);
            }
        }

        return resList;
    }

}
