package ru.yaklimenko.fuel.db.dao;

import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import ru.yaklimenko.fuel.db.DbHelperManager;
import ru.yaklimenko.fuel.db.entities.Fuel;

/**
 * Created by Антон on 29.05.2016.
 * dao for fuel
 */
public class FuelDao extends FuelBaseDao<Fuel, Integer> {

    public static FuelDao getInstance() {
        try {
            return new FuelDao(DbHelperManager.getHelper().getConnectionSource(), Fuel.class);
        } catch (SQLException e) {
            throw new IllegalStateException("cannot get dao", e);
        }
    }

    protected FuelDao(ConnectionSource connectionSource, Class<Fuel> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public List<Fuel> getByStation (int stationsId) {
        try {
            return queryBuilder().where().eq(Fuel.COL_STATION, stationsId).query();
        } catch (SQLException e) {
            throw new IllegalStateException("cannot get fuels by stations", e);
        }
    }

    public void createAll(final List<Fuel> fuels) {
        callBatchTasks(new Callable<Boolean>() {
            public Boolean call() {
                for (Fuel fuel : fuels) {
                    create(fuel);
                }
                return true;
            }
        });

    }

    public List<Fuel> getByCategory (int fuelCategoryId, boolean isSortedByPriceAsc) {
        try {
            return queryBuilder()
                    .orderBy(Fuel.COL_PRICE, isSortedByPriceAsc)
                    .where()
                    .eq(Fuel.COL_CATEGORY, fuelCategoryId)
                    .query();
        } catch (SQLException e) {
            throw new IllegalStateException("cannot get fuel list", e);
        }
    }

    public List<Fuel> getByCategory (int fuelCategoryId) {
        try {
            return queryBuilder().where().eq(Fuel.COL_CATEGORY, fuelCategoryId).query();
        } catch (SQLException e) {
            throw new IllegalStateException("cannot get fuel list", e);
        }
    }

}
