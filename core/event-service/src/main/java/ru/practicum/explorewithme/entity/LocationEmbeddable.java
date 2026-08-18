package ru.practicum.explorewithme.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LocationEmbeddable {
    @Column(name = "location_lat")
    private Double lat;

    @Column(name = "location_lon")
    private Double lon;
}
