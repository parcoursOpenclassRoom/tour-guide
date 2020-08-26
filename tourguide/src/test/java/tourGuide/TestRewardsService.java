package tourGuide;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.gps.Attraction;
import tourGuide.model.gps.VisitedLocation;
import tourGuide.repository.GpsUtilRepository;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.model.user.User;
import tourGuide.model.user.UserReward;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRewardsService {

    @Autowired
    GpsUtilRepository gpsUtilRepository;
	@Autowired
	AutowireCapableBeanFactory beanFactory;


	@Test
	public void userGetRewards() throws ExecutionException, InterruptedException {
		RewardsService rewardsService = new RewardsService();
		beanFactory.autowireBean(rewardsService);
		TourGuideService tourGuideService = new TourGuideService(rewardsService);
		beanFactory.autowireBean(tourGuideService);

		InternalTestHelper.setInternalUserNumber(0);
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = gpsUtilRepository.getAttraction().get(0);
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		tourGuideService.trackUserLocation(user).get();
		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.tracker.stopTracking();
		assertTrue(userRewards.size() > 0);
	}
	
	@Test
	public void isWithinAttractionProximity() {
		RewardsService rewardsService = new RewardsService();
		beanFactory.autowireBean(rewardsService);
		TourGuideService tourGuideService = new TourGuideService(rewardsService);
		beanFactory.autowireBean(tourGuideService);

		Attraction attraction = gpsUtilRepository.getAttraction().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}
	
	@Test
	public void nearAllAttractions() throws ExecutionException, InterruptedException {
		RewardsService rewardsService = new RewardsService();
		beanFactory.autowireBean(rewardsService);
		TourGuideService tourGuideService = new TourGuideService(rewardsService);
		beanFactory.autowireBean(tourGuideService);

		rewardsService.setProximityBuffer(Integer.MAX_VALUE);

		InternalTestHelper.setInternalUserNumber(1);
		tourGuideService.initializeInternalUsers();
		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));
		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
		tourGuideService.tracker.stopTracking();
		assertEquals(gpsUtilRepository.getAttraction().size(), userRewards.size());
	}

}
