package ru.yaklimenko.fuel.db.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Антон on 21.05.2016.
 */
@DatabaseTable
public class Fuel {
    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField(index = true)
    public int stationId;

    @DatabaseField
    public int categoryId;

    @DatabaseField
    public float price;

}
