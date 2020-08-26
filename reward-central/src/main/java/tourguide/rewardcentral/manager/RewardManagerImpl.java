package tourguide.rewardcentral.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.UUID;
@Service
public class RewardManagerImpl implements RewardManager {
    @Autowired
    RewardCentral rewardCentral;
    @Override
    public int getRewardPoints(UUID attraction, UUID user) {
        return rewardCentral.getAttractionRewardPoints(attraction, user);
    }
}
