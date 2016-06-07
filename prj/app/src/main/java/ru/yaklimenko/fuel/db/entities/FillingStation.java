package ru.yaklimenko.fuel.db.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;



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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FillingStation that = (FillingStation) o;

        if (id != that.id) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (!name.equals(that.name)) return false;
        if (!latitude.equals(that.latitude)) return false;
        return longitude.equals(that.longitude);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + name.hashCode();
        result = 31 * result + latitude.hashCode();
        result = 31 * result + longitude.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "|" + name + "|" + address + "|";
    }
}
