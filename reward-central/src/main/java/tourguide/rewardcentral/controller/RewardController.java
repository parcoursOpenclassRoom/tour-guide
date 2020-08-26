package tourguide.rewardcentral.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tourguide.rewardcentral.manager.RewardManager;

import java.util.UUID;

@RestController
public class RewardController {
    @Autowired
    RewardManager rewardManager;

    @GetMapping("/reward-point")
    public int getRewardPoints(@RequestParam UUID attraction, @RequestParam UUID user){
        return rewardManager.getRewardPoints(attraction, user);
    }
}
