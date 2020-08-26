package tourGuide;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.javamoney.moneta.Money;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.gps.Attraction;
import tourGuide.model.gps.VisitedLocation;
import tourGuide.model.user.UserPreferences;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.model.user.User;
import tripPricer.Provider;

import javax.money.Monetary;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestTourGuideService {
    @Autowired
    AutowireCapableBeanFactory beanFactory;

	@Test
	public void getUserLocation() throws ExecutionException, InterruptedException {
        RewardsService rewardsService = new RewardsService();
        beanFactory.autowireBean(rewardsService);
        TourGuideService tourGuideService = new TourGuideService(rewardsService);
        beanFactory.autowireBean(tourGuideService);

		InternalTestHelper.setInternalUserNumber(0);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).get();
		tourGuideService.tracker.stopTracking();
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}
	
	@Test
	public void addUser() {
        RewardsService rewardsService = new RewardsService();
        beanFactory.autowireBean(rewardsService);
        TourGuideService tourGuideService = new TourGuideService(rewardsService);
        beanFactory.autowireBean(tourGuideService);
		InternalTestHelper.setInternalUserNumber(0);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

		tourGuideService.tracker.stopTracking();
		
		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);
	}
	
	@Test
	public void getAllUsers() {
        RewardsService rewardsService = new RewardsService();
        beanFactory.autowireBean(rewardsService);
        TourGuideService tourGuideService = new TourGuideService(rewardsService);
        beanFactory.autowireBean(tourGuideService);
		InternalTestHelper.setInternalUserNumber(0);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		List<User> allUsers = tourGuideService.getAllUsers();

		tourGuideService.tracker.stopTracking();
		
		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}
	
	@Test
	public void trackUser() throws ExecutionException, InterruptedException {
        RewardsService rewardsService = new RewardsService();
        beanFactory.autowireBean(rewardsService);
        TourGuideService tourGuideService = new TourGuideService(rewardsService);
        beanFactory.autowireBean(tourGuideService);
		InternalTestHelper.setInternalUserNumber(0);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).get();
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(user.getUserId(), visitedLocation.userId);
	}
	
	@Test
	public void getNearbyAttractions() throws ExecutionException, InterruptedException {
        RewardsService rewardsService = new RewardsService();
        beanFactory.autowireBean(rewardsService);
        TourGuideService tourGuideService = new TourGuideService(rewardsService);
        beanFactory.autowireBean(tourGuideService);
		InternalTestHelper.setInternalUserNumber(0);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).get();
		
		List<Attraction> attractions = tourGuideService.getNearByAttractions(visitedLocation, user);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(5, attractions.size());
	}

	@Test
	public void getTripDeals() {
        RewardsService rewardsService = new RewardsService();
        beanFactory.autowireBean(rewardsService);
        TourGuideService tourGuideService = new TourGuideService(rewardsService);
        beanFactory.autowireBean(tourGuideService);
		InternalTestHelper.setInternalUserNumber(0);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		List<Provider> providers = tourGuideService.getTripDeals(user);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(5, providers.size());
	}

	@Test
	public void preferencesTest() {
        RewardsService rewardsService = new RewardsService();
        beanFactory.autowireBean(rewardsService);
        TourGuideService tourGuideService = new TourGuideService(rewardsService);
        beanFactory.autowireBean(tourGuideService);
		InternalTestHelper.setInternalUserNumber(0);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		String userName = "jon";
		UserPreferences userPreferences = new UserPreferences();
		userPreferences.setAttractionProximity(2147483647);
		userPreferences.setLowerPricePoint(Money.of(100, Monetary.getCurrency("USD")));
		userPreferences.setNumberOfAdults(1);
		userPreferences.setNumberOfChildren(1);
		userPreferences.setTicketQuantity(1);
		tourGuideService.addUser(user);
		// add preferences
		assertNotNull(tourGuideService.addPreferences(userName, userPreferences));
		// get preferences
		assertNotNull(tourGuideService.getPreferences(userName));
	}

	@Test
	public void getAllCurrentLocations() {
        RewardsService rewardsService = new RewardsService();
        beanFactory.autowireBean(rewardsService);
        TourGuideService tourGuideService = new TourGuideService(rewardsService);
        beanFactory.autowireBean(tourGuideService);
		InternalTestHelper.setInternalUserNumber(100);
		List<User> allUsers = tourGuideService.getAllUsers();
		assertEquals(allUsers.size(), tourGuideService.getAllCurrentLocations().size());
	}

}
