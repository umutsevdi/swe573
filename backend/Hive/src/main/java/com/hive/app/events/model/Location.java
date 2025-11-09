package com.hive.app.events.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Location {
    @JsonProperty("lat")
    private Double latitude;
    @JsonProperty("lon")
    private Double longitude;
    @JsonProperty("name")
    private String name;
}
