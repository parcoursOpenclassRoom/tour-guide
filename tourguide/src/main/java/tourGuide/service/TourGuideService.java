package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tourGuide.helper.InternalTestHelper;
import tourGuide.model.gps.Attraction;
import tourGuide.model.gps.Location;
import tourGuide.model.gps.VisitedLocation;
import tourGuide.repository.GpsUtilRepository;
import tourGuide.tracker.Tracker;
import tourGuide.model.user.User;
import tourGuide.model.user.UserPreferences;
import tourGuide.model.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	@Autowired
	private GpsUtilRepository gpsUtilRepository;

	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	// private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;
	
	public TourGuideService(RewardsService rewardsService) {
		// this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		
		if(testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	public VisitedLocation getUserLocation(User user) throws ExecutionException, InterruptedException {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
			trackUserLocation(user).get();
		return visitedLocation;
	}
	
	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}
	
	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}
	
	public void addUser(User user) {
		if(!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}
	
	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(), 
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
		return CompletableFuture.supplyAsync(() -> {
			VisitedLocation visitedLocation = gpsUtilRepository.getUserLocation(user.getUserId());
			user.addToVisitedLocations(visitedLocation);
			try {
				rewardsService.calculateRewards(user);
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return visitedLocation;
		});
	}

	/**
	 * returns the 5 nearby attractions from the GPS coordinates
	 * @param visitedLocation
	 * @return
	 */
	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation, User user) {
		List nearbyAttractions = new ArrayList<>();
		Map<Double, Attraction> attractionsProximity = new TreeMap<>();
		for (Attraction attraction : gpsUtilRepository.getAttraction()) {
			attractionsProximity.put(rewardsService.getDistance(attraction, visitedLocation.location), attraction);
		}
		attractionsProximity.forEach((distance, attraction) -> {
			if (nearbyAttractions.size() < 5) {
				Map nearAttractions = new HashMap();
				nearAttractions.put("name", attraction.attractionName);
				Map<String, Double> attractionsLocation = new HashMap<>();
				Map<String, Double> userLocation = new HashMap<>();
				attractionsLocation.put("latitude", attraction.latitude);
				attractionsLocation.put("longitude", attraction.longitude);
				nearAttractions.put("attractionsLocation", attractionsLocation);
				userLocation.put("latitude", visitedLocation.location.latitude);
				userLocation.put("longitude", visitedLocation.location.longitude);
				nearAttractions.put("userLocation", userLocation);
				nearAttractions.put("distance", rewardsService.getDistance(attraction, visitedLocation.location));
				nearAttractions.put("points", rewardsService.getRewardPoints(attraction, user));
				nearbyAttractions.add(nearAttractions);
			}
		});
		return nearbyAttractions;
	}
	
	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() { 
		      public void run() {
		        tracker.stopTracking();
		      } 
		    }); 
	}
	
	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();
	public void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);
			
			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}
	
	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i-> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}
	
	private double generateRandomLongitude() {
		double leftLimit = -180;
	    double rightLimit = 180;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
	    double rightLimit = 85.05112878;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
	    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

	/**
	 * add user preferences
	 * @param userName
	 * @param userPreferences
	 * @return
	 */
	public UserPreferences addPreferences(String userName, UserPreferences userPreferences) {
		if(getAllUsers().size() == 0)
			initializeInternalUsers();
		User user = getUser(userName);
		if(user != null)
			user.setUserPreferences(userPreferences);
		return userPreferences;
	}

	/**
	 * retrieve a user's preferences
	 * @param userName
	 * @return
	 */
	public UserPreferences getPreferences(String userName) {
		User user = getUser(userName);
		return user != null ? user.getUserPreferences() : null;
	}

	public Map<UUID, Location> getAllCurrentLocations() {
		Map<UUID, Location> loc = new HashMap();
		for (User user: getAllUsers()){
			Location location = user.getLastVisitedLocation() != null ? user.getLastVisitedLocation().location : null;
			loc.put(user.getUserId(),location );
		}
		return loc;
	}
}
