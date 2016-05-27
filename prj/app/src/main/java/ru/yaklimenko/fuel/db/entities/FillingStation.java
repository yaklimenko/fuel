package ru.yaklimenko.fuel.db.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class FillingStation {

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


}
