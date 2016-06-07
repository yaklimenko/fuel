package ru.yaklimenko.fuel.db.dao;

import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import ru.yaklimenko.fuel.db.DbHelperManager;
import ru.yaklimenko.fuel.db.entities.FuelCategory;

/**
 * Created by Антон on 29.05.2016.
 * dao for fuel categories
 */
public class FuelCategoryDao extends FuelBaseDao<FuelCategory, Integer> {

    public static FuelCategoryDao getInstance() {
        try {
            return new FuelCategoryDao(DbHelperManager.getHelper().getConnectionSource(), FuelCategory.class);
        } catch (SQLException e) {
            throw new IllegalStateException("cannot get dao", e);
        }
    }

    public FuelCategoryDao(ConnectionSource connectionSource, Class<FuelCategory> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public void createAll (final List<FuelCategory> categories) {
        callBatchTasks(new Callable<Boolean>() {
            public Boolean call() {
                for (FuelCategory cat : categories) {
                    create(cat);
                }
                return true;
            }
        });
    }

    public FuelCategory getByName (String fuelCategoryName) {
        try {
            return queryBuilder()
                    .where()
                    .eq(FuelCategory.COL_NAME, fuelCategoryName)
                    .queryForFirst();
        } catch (SQLException e) {
            throw new IllegalStateException("cannot get fuel category by name", e);
        }
    }

    public FuelCategory getFirst () {
        try {
            return queryBuilder().queryForFirst();
        } catch (SQLException e) {
            throw new IllegalStateException("cannot get first fuel", e);
        }
    }

    public List<FuelCategory> getAllSorted() {
        try {
            return queryBuilder().orderBy(FuelCategory.COL_NAME, true).query();
        } catch (SQLException e) {
            throw new IllegalStateException("cannot get all fuel categories", e);
        }
    }
}
