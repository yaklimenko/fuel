package ru.yaklimenko.fuel.db.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

import ru.yaklimenko.fuel.db.entities.FillingStation;

/**
 * Created by Антон on 26.05.2016.
 * Dao class for filling stations entity
 */
public class FillingStationDao extends BaseDaoImpl<FillingStation, Integer> {

    protected FillingStationDao(ConnectionSource connectionSource, Class<FillingStation> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }


}
