package ru.yaklimenko.fuel.dto;

import com.google.gson.annotations.SerializedName;


public class FillingStationsMeta {
    int limit;
    int offset;
    @SerializedName("total_count")
    int totalCount;
}
