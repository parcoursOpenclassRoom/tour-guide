package tourGuide.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.jsoniter.output.JsonStream;

import tourGuide.model.gps.Location;
import tourGuide.model.gps.VisitedLocation;
import tourGuide.service.TourGuideService;
import tourGuide.model.user.User;
import tourGuide.model.user.UserPreferences;
import tripPricer.Provider;

@RestController
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;
	
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    /**
     * save a user's preferences from his username
     * @param userName
     * @param userPreferences
     * @return
     */
    @PostMapping("/add-user-preferences")
    public UserPreferences addPreferences(@RequestParam String userName, @RequestBody UserPreferences userPreferences) {
        return tourGuideService.addPreferences(userName, userPreferences);
    }

    /**
     * get user's preferences from his username
     * @param userName
     * @return
     */
    @GetMapping("/get-user-preferences")
    public UserPreferences getPreferences(@RequestParam String userName) {
        return tourGuideService.getPreferences(userName);
    }
    
    @RequestMapping("/getLocation") 
    public String getLocation(@RequestParam String userName) throws ExecutionException, InterruptedException {
        User user = getUser(userName);
        if(user == null)
            return null;
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);
		return JsonStream.serialize(visitedLocation.location);
    }

    /**
     * retrieve the 5 attractions close to the user
     * @param userName
     * @return
     */
    @RequestMapping("/getNearbyAttractions") 
    public List getNearbyAttractions(@RequestParam String userName) throws ExecutionException, InterruptedException {
        //  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
        //  Return a new JSON object that contains:
        // Name of Tourist attraction,
        // Tourist attractions lat/long,
        // The user's location lat/long,
        // The distance in miles between the user's location and each of the attractions.
        // The reward points for visiting each Attraction.
        //    Note: Attraction reward points can be gathered from RewardsCentral
        User user = getUser(userName);
        if(user == null)
            return null;
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);
        return tourGuideService.getNearByAttractions(visitedLocation, user);
    }
    
    @RequestMapping("/getRewards") 
    public String getRewards(@RequestParam String userName) {
        User user = getUser(userName);
        if(user == null)
            return null;
    	return JsonStream.serialize(tourGuideService.getUserRewards(user));
    }
    
    @RequestMapping("/getAllCurrentLocations")
    public Map<UUID, Location> getAllCurrentLocations() {
    	//- Note: does not use gpsUtil to query for their current location,
    	//        but rather gathers the user's current location from their stored location history.
    	//
    	// Return object should be the just a JSON mapping of userId to Locations similar to:
    	//     {
    	//        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371} 
    	//        ...
    	//     }

        return tourGuideService.getAllCurrentLocations();
    }
    
    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {
        User user = getUser(userName);
        if(user == null)
            return null;
    	List<Provider> providers = tourGuideService.getTripDeals(user);
    	return JsonStream.serialize(providers);
    }
    
    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }
   

}