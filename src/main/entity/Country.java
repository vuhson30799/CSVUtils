package main.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Country {
    private Map<String, String> calculatedData;
    private String name;

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Error when parsing object to country.");
        }
        return this.getName().equals(((Country) object).getName());
    }
}
