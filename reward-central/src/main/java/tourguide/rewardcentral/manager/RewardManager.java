package tourguide.rewardcentral.manager;


import java.util.UUID;

public interface RewardManager {
    int getRewardPoints(UUID attraction, UUID user);
}
