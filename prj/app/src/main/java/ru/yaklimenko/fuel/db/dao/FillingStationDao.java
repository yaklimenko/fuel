package ru.yaklimenko.fuel.db.dao;

import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import ru.yaklimenko.fuel.db.DbHelperManager;
import ru.yaklimenko.fuel.db.entities.FillingStation;

/**
 * Created by Антон on 26.05.2016.
 * Dao class for filling stations entity
 */
public class FillingStationDao extends FuelBaseDao<FillingStation, Integer> {

    public static FillingStationDao getInstance() {
        try {
            return new FillingStationDao(DbHelperManager.getHelper().getConnectionSource(), FillingStation.class);
        } catch (SQLException e) {
            throw new IllegalStateException("cannot get dao", e);
        }
    }

    protected FillingStationDao(ConnectionSource connectionSource, Class<FillingStation> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public void createAll(final List<FillingStation> stations) {
        callBatchTasks(new Callable<Boolean>() {
            public Boolean call() {
                for (FillingStation station : stations) {
                    create(station);
                }
                return true;
            }
        });

    }

}
