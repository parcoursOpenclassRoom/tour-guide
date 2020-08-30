package tourGuide.repository;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "microservice-reward-central", url = "localhost:8083")
public interface RewardCentralRepository {

    @GetMapping("/reward-point")
     int getRewardPoints(@RequestParam UUID attraction, @RequestParam UUID user);
}
