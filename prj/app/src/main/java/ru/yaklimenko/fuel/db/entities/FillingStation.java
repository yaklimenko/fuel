package ru.yaklimenko.fuel.db.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import ru.yaklimenko.fuel.db.DBHelper;

@DatabaseTable
public class FillingStation {

    public static final String REQUEST_URL = "fillingstation";

    @DatabaseField(id = true, dataType = DataType.INTEGER)
    public int id;

    @DatabaseField
    public String address;

    @DatabaseField
    public String name;

    @DatabaseField(canBeNull = false)
    public Double latitude;

    @DatabaseField(canBeNull = false)
    public Double longitude;

    public Fuel[] fuels;


}
