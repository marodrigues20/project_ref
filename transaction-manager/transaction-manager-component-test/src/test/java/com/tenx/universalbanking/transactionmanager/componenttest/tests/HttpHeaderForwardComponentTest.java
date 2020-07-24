package com.tenx.universalbanking.transactionmanager.componenttest.tests;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.stubLMProcessPayments;
import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.stubPaymentDecisionSwaggerClient;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.FileUtils.getFileContent;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tenx.universalbanking.transactionmanager.componenttest.BaseComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.retry.Retry;
import java.util.Collections;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureMockMvc
public class HttpHeaderForwardComponentTest extends BaseComponentTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String TRANSACTION_MANAGER_END_POINT = "/transaction-manager/process-payment";
    private static final String TRANSACTION_MESSAGE_REQUEST = "data/request/fpsout/FPS_Out-TransactionMessageRequest.json";
    private static final String LEDGER_MANAGER_PROCESS_PAYMENTS = "/ledger-manager/v1/processPayments";

    @After
    public void tearDown() {
        WIREMOCK_SERVER.resetAll();
    }

    // No need for database tests as it is not database dependent

    //@ActiveProfiles("mysql")
    //public static class HttpHeaderForwardSqlTest extends HttpHeaderForwardComponentTest {}
    //@ActiveProfiles("cockroachdb")
    //public static class HttpHeaderForwardCrTest extends HttpHeaderForwardComponentTest {}

    @Test @Retry
    public void httpHeaderForwardTMRequest_ToDownstreamAPI_VerifyingHeaders() throws Exception {
        stubPaymentDecisionSwaggerClient(WIREMOCK_SERVER, true);
        stubLMProcessPayments(WIREMOCK_SERVER);
        final String request = getFileContent(TRANSACTION_MESSAGE_REQUEST);

        HttpHeaders header = new HttpHeaders();
        header.put("x-ray-trace", Collections.singletonList("123"));
        header.put("l5d-1", Collections.singletonList("ABC"));

        mockMvc.perform(
                MockMvcRequestBuilders.post(TRANSACTION_MANAGER_END_POINT)
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .headers(header))
                .andExpect(status().isOk());

        verifyHeaders(LEDGER_MANAGER_PROCESS_PAYMENTS);
    }

    private void verifyHeaders(String url) {
        WIREMOCK_SERVER.verify(postRequestedFor(urlEqualTo(url))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("x-ray-trace", equalTo("123"))
                .withHeader("l5d-1", equalTo("ABC")));
    }
}