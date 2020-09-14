package main.entity;

import lombok.Getter;

@Getter
public enum LocationType {
    LOCATION_OF_PRESENCE("LOCATION_OF_PRESENCE"),
    AREA_OF_INTEREST("AREA_OF_INTEREST");

    private String value;

    LocationType(String value) {
        this.value = value;
    }
}
