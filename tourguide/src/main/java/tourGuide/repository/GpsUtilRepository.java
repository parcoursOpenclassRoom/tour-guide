package tourGuide.repository;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tourGuide.model.gps.Attraction;
import tourGuide.model.gps.VisitedLocation;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "microservice-gps-util", url = "localhost:8082")
public interface GpsUtilRepository {

    @GetMapping("/attraction")
    List<Attraction> getAttraction();

    @GetMapping("/get-user-location")
    VisitedLocation getUserLocation(@RequestParam UUID userId);
}
