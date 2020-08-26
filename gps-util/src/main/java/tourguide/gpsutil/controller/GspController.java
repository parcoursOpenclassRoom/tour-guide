package tourguide.gpsutil.controller;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tourguide.gpsutil.service.GpsUtilManager;

import java.util.List;
import java.util.UUID;

@RestController
public class GspController {
    @Autowired
    GpsUtilManager gpsUtilManager;

    @GetMapping("/attraction")
    public List<Attraction> getAttraction(){
        return gpsUtilManager.getAttractions();
    }

    @GetMapping("/get-user-location")
    public VisitedLocation getUserLocation(@RequestParam UUID userId){
        return gpsUtilManager.getUserLocation(userId);
    }

}
