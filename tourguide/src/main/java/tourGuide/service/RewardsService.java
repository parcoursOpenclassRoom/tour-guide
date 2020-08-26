package tourGuide.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tourGuide.model.gps.Attraction;
import tourGuide.model.gps.Location;
import tourGuide.model.gps.VisitedLocation;
import tourGuide.model.user.User;
import tourGuide.model.user.UserReward;
import tourGuide.repository.GpsUtilRepository;
import tourGuide.repository.RewardCentralRepository;

@Service
public class RewardsService {
	@Autowired
	GpsUtilRepository gpsUtilRepository;
	@Autowired
	RewardCentralRepository rewardCentralRepository;

    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	//private final GpsUtil gpsUtil;

	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	/**
	 * Determine the rewards to be awarded to each user
	 * @param user
	 */
	public void calculateRewards(User user) throws ExecutionException, InterruptedException {
		CompletableFuture future = CompletableFuture.runAsync(() -> {
			CopyOnWriteArrayList<Attraction> attractions = new CopyOnWriteArrayList<>();
			CopyOnWriteArrayList<VisitedLocation> userLocations = new CopyOnWriteArrayList<>();
			attractions.addAll(gpsUtilRepository.getAttraction());
			userLocations.addAll(user.getVisitedLocations());
			for(VisitedLocation visitedLocation : userLocations) {
				for(Attraction attraction : attractions) {
					if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
						if(nearAttraction(visitedLocation, attraction)) {
							user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
						}
					}
				}
			}
		});
		future.get();
	}
	
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}

	/**
	 * points for visiting each Attraction.
	 * @param attraction
	 * @param user
	 * @return
	 */
	public int getRewardPoints(Attraction attraction, User user) {
		return rewardCentralRepository.getRewardPoints(attraction.attractionId, user.getUserId());
	}
	
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}

}
