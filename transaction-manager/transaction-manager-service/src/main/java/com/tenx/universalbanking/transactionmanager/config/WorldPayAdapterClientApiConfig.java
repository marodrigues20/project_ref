package com.tenx.universalbanking.transactionmanager.config;

import com.tenx.universalbanking.transactionmanager.client.worldpayadapter.ApiClient;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
class WorldPayAdapterClientApiConfig {

	@Value("${downstream.worldpay-adapter.rest.url}")
	private String worldPayAdapterBasePath;

	@Autowired
	private ApiClient worldPayAdapterClient;

	@PostConstruct
	public void setBasePath() {
		worldPayAdapterClient.setBasePath(worldPayAdapterBasePath);
	}

}
