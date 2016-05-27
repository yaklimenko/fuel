package ru.yaklimenko.fuel.db.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Антон on 27.05.2016.
 */
public class FuelBaseDao<T, ID> extends BaseDaoImpl<T, ID> {
    protected FuelBaseDao(ConnectionSource connectionSource, Class<T> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public List<T> queryForAll() {
        try {
            return super.queryForAll();
        } catch (SQLException e) {
            throw new IllegalStateException("cannot query for all", e);
        }
    }

    @Override
    public T queryForId(ID id) {
        try {
            return super.queryForId(id);
        } catch (SQLException e) {
            throw new IllegalStateException("cannot query for id", e);
        }
    }


}
