package ru.yaklimenko.fuel.db.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by Антон on 21.05.2016.
 * fuel category
 */
@DatabaseTable
public class FuelCategory implements Serializable {

    public static final String REQUEST_URL = "category";

    @DatabaseField(id = true)
    public int id;

    @DatabaseField
    public String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FuelCategory that = (FuelCategory) o;

        if (id != that.id) return false;
        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        return result;
    }
}
