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
}
