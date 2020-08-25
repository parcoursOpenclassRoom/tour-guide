package tourguide.gpsutil.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

import java.util.List;
import java.util.UUID;

public interface GpsUtilManager {
    List<Attraction> getAttractions();

    VisitedLocation getUserLocation(UUID userId);
}
