package com.tenx.universalbanking.transactionmanager.config;

import com.tenx.universalbanking.transactionmanager.client.ledgermanager.ApiClient;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
class LedgerManagerClientApiConfig {

	@Value("${downstream.ledger-manager.rest.url}")
	private String ledgerManagerBasePath;

	@Autowired
	private ApiClient ledgerManagerClient;

	@PostConstruct
	public void setBasePath() {
    ledgerManagerClient.setBasePath(ledgerManagerBasePath);
	}

}
