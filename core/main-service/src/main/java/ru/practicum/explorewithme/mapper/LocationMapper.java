package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.event.Location;
import ru.practicum.explorewithme.entity.LocationEmbeddable;

public class LocationMapper {
    public static Location toLocation(LocationEmbeddable locationEmbeddable) {
        return new Location(locationEmbeddable.getLat(), locationEmbeddable.getLon());
    }

    public static LocationEmbeddable toLocationEmbeddable(Location location) {
        return new LocationEmbeddable(location.lat(), location.lon());
    }
}
