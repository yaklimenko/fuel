package ru.yaklimenko.fuel.db.entities;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Антон on 21.05.2016.
 */
@DatabaseTable
public class Fuel {

    public static final String COL_STATION = "stationId";
    public static final String COL_CATEGORY = "categoryId";
    public static final String COL_PRICE = "price";

    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField(index = true, columnName = COL_STATION)
    public int stationId;

    @SerializedName("category_id")
    @DatabaseField (columnName = COL_CATEGORY)
    public int categoryId;

    @DatabaseField(columnName = COL_PRICE)
    public double price;

}
