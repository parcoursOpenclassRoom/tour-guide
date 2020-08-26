package tourguide.rewardcentral.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rewardCentral.RewardCentral;

@Configuration
public class RewardConfig {

	@Bean
	public RewardCentral getRewardCentral() {
		return new RewardCentral();
	}

}
