package ru.yaklimenko.fuel.dto;

import ru.yaklimenko.fuel.db.entities.FillingStation;

public class FillingStationsWrapper {
    public ServerResponseMeta meta;
    public FillingStation[] objects;
}
