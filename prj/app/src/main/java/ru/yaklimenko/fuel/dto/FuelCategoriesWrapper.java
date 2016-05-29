package ru.yaklimenko.fuel.dto;

import ru.yaklimenko.fuel.db.entities.FuelCategory;

/**
 * Created by Антон on 29.05.2016.
 */
public class FuelCategoriesWrapper {

    public ServerResponseMeta meta;
    public FuelCategory[] objects;

}
