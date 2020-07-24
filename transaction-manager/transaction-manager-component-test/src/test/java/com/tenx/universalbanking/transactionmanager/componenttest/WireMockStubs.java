package com.tenx.universalbanking.transactionmanager.componenttest;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.tenx.universalbanking.transactionmanager.componenttest.utils.FileUtils;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.Constants.APPLICATION_JSON;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.Constants.CONTENT_TYPE;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.FileUtils.getFileContent;

public class WireMockStubs {

  private static final String GENERIC_RESPONSE = "data/response/CommonResponse.json";
  private static final String PARTY_ACCOUNT_PERIOD_RESPONSE = "data/response/PartyAccountPeriodCloseDateResponse.json";
  private static final String PARTY_TENANT_RESPONSE = "data/response/PartyTenantResponse.json";
  private static final String CATEGORISATION_RESPONSE = "data/response/CategorisationResponse.json";
  private static final String POSITIVE_PAYMENT_DECISION_RESPONSE = "data/response/PaymentPositiveDecisionResponse.json";
  private static final String NEGATIVE_PAYMENT_DECISION_RESPONSE = "data/response/PaymentNegativeDecisionResponse.json";
  private static final String STUB_POSITIVE_PAYMENT_DECISION_RESPONSE = "data/response/FPSOutPaymentPositiveDecision.json";
  private static final String FPS_OUT_RESPONSE = "data/response/FPSOutResponse.json";
  private static final String STUB_NEGATIVE_PAYMENT_DECISION_RESPONSE = "data/response/FPSOutPaymentNegativeDecision.json";
  private static final String SUBSCRIPTION_BY_PAN_RESPONSE = "data/response/SubscriptionByPanResponse.json";
  private static final String SUBSCRIPTION_BY_ACCNO_SORTCODE_RESPONSE = "data/response/SubscriptionByAccNoSortCodeResponse.json";
  private static final String PDF_SUCCESS_RESPONSE = "data/response/PDFSuccessResponse.json";
  private static final String PDF_RESERVE_FAILURE_RESPONSE = "data/response/PDFReserveFailureResponse.json";

  private static final String PDF_FPSOUT_AMOUNT_EXCEED_FAILURE_RESPONSE = "data/response/fpsout/PdfFPSOutAmountExceedFailure.json";
  private static final String PDF_FPSOUT_AMOUNT_LESS_ZERO_FAILURE_RESPONSE = "data/response/fpsout/PdfFPSOutAmountLessZeroFailure.json";

  private static final String PDF_RULES_FAILED_RESPONSE = "data/response/PDFRulesFailedResponse.json";
  private static final String PAYMENT_DECISION_SUCCESS_RESPONSE = "data/response/payment-decision-success-response.json";
  private static final String PAYMENT_DECISION_FAILURE_RESPONSE = "data/response/payment-decision-failure-response.json";
  private static final String WORLD_PAY_ADAPTER_SUCCESS_RESPONSE = "data/response/cardfund/worldpay-adapter-success-response.json";
  private static final String WORLD_PAY_ADAPTER_FAILURE_REFUSED_RESPONSE = "data/response/cardfund/worldpay-adapter-failure-refused-response.json";
  private static final String LM_PROCESS_PAYMENTS_SUCCESS = "data/response/LedgerProcessPaymentsResponse.json";

  private static final String FORM3_SERVER_ERROR_RESPONSE = "data/response/fpsout/FPSOut_Form3_Server_Error_Response.json";

  private static final String FAM_REQUEST_PAYAMENT_SUCCESS_RESPONSE = "data/response/request-payment/fam_success_response.json";
  private static final String FAM_REQUEST_PAYAMENT_FAILURE_RESPONSE = "data/response/request-payment/fam_failure_response.json";
  private static final String FAM_REQUEST_PAYAMENT_3D_SECURE_RESPONSE = "data/response/request-payment/fam_3dsecure_response.json";

  private static final FileUtils fileUtils = new FileUtils();

  public static void stubPartyManagerTenant(WireMockServer server) throws IOException {
    server.stubFor(get(urlEqualTo("/party-manager/tenant/10000"))
        .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(fileUtils.getFileContent(PARTY_TENANT_RESPONSE))));
  }

  public static void stubPaymentDecisionMakePaymentDecision(WireMockServer server,
      boolean positiveDecision) throws IOException {

    String file = (positiveDecision ? POSITIVE_PAYMENT_DECISION_RESPONSE
        : NEGATIVE_PAYMENT_DECISION_RESPONSE);

    server.stubFor(post(urlEqualTo("/payment-decision/make-payment-decision"))
        .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(fileUtils.getFileContent(file))));
  }

  public static void stubPaymentDecisionWithGatewayTimeoutStatus(WireMockServer server) {
    server.stubFor(post(urlEqualTo("/payment-decision/v1/payment-decision")).willReturn(aResponse()
        .withHeader(CONTENT_TYPE, APPLICATION_JSON).withStatus(HttpStatus.GATEWAY_TIMEOUT_504)));
  }

  public static void stubPaymentDecisionFpsOutAmountExceedFailure(WireMockServer server)
      throws IOException {
    server.stubFor(post(urlEqualTo("/payment-decision/v1/payment-decision"))
        .willReturn(
            aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withStatus(HttpStatus.OK_200)
                .withBody(fileUtils.getFileContent(PDF_FPSOUT_AMOUNT_EXCEED_FAILURE_RESPONSE))));
  }

  public static void stubPaymentDecisionFpsOutAmountLessZeroFailure(WireMockServer server)
      throws IOException {
    server.stubFor(post(urlEqualTo("/payment-decision/v1/payment-decision"))
        .willReturn(
            aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withStatus(HttpStatus.OK_200)
                .withBody(fileUtils.getFileContent(PDF_FPSOUT_AMOUNT_LESS_ZERO_FAILURE_RESPONSE))));
  }

  public static void stubPaymentProxyWithGatewayTimeoutStatus(WireMockServer server,
      boolean positiveDecision)
      throws IOException {

    String file = (positiveDecision ? STUB_POSITIVE_PAYMENT_DECISION_RESPONSE
        : STUB_NEGATIVE_PAYMENT_DECISION_RESPONSE);

    server.stubFor(post(urlEqualTo("/payment-decision/v1/payment-decision")).willReturn(aResponse()
        .withHeader(CONTENT_TYPE, APPLICATION_JSON).withBody(fileUtils.getFileContent(file))));

    server.stubFor(post(urlEqualTo("/payment-proxy/v1/payment/fps/out"))
        .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withStatus(HttpStatus.GATEWAY_TIMEOUT_504)));
  }

  public static void stubPaymentDecisionSwaggerClient(WireMockServer server,
      boolean positiveDecision)
      throws IOException {

    String file = (positiveDecision ? STUB_POSITIVE_PAYMENT_DECISION_RESPONSE
        : STUB_NEGATIVE_PAYMENT_DECISION_RESPONSE);

    server.stubFor(post(urlEqualTo("/payment-decision/v1/payment-decision")).willReturn(aResponse()
        .withHeader(CONTENT_TYPE, APPLICATION_JSON).withBody(fileUtils.getFileContent(file))));

    server.stubFor(post(urlEqualTo("/payment-proxy/v1/payment/fps/out"))
        .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(fileUtils.getFileContent(FPS_OUT_RESPONSE))));
  }

  public static void stubPaymentDecisionV1(WireMockServer server, boolean positiveDecision)
      throws IOException {
    String file = (positiveDecision ? PDF_SUCCESS_RESPONSE : PDF_RESERVE_FAILURE_RESPONSE);

    server.stubFor(post(urlEqualTo("/payment-decision/v1/payment-decision")).willReturn(aResponse()
        .withHeader(CONTENT_TYPE, APPLICATION_JSON))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
            .withBody(fileUtils.getFileContent(file))));
  }

  public static void stubPaymentProxyForm3ServerError(WireMockServer server) throws IOException {
    server.stubFor(post(urlEqualTo("/payment-proxy/v1/payment/fps/out"))
        .willReturn(aResponse().withStatus(500)
            .withBody(fileUtils.getFileContent(FORM3_SERVER_ERROR_RESPONSE))));
  }

  public static void stubGetBalancesUsingGET(WireMockServer server)
      throws IOException {
    String file = "data/ledger-messages/cardauthadvice/LMGetBalanceResponseMessage.json";
    server.stubFor(get(urlMatching("/ledger-manager/v2/accounts/([a-f0-9\\-]*)/balances"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
            .withBody(fileUtils.getFileContent(file))));
  }

  public static void stubLMProcessPayments(WireMockServer server)
      throws IOException {
    String file = "data/ledger-messages/cardauth/Cain002TransactionMessage.json";
    server.stubFor(post(urlEqualTo("/ledger-manager/v1/processPayments")).willReturn(aResponse()
        .withHeader(CONTENT_TYPE, APPLICATION_JSON))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
            .withBody(fileUtils.getFileContent(file))));
  }

  public static void stubDCM(WireMockServer server)
      throws IOException {
    server.stubFor(get(urlEqualTo("/v1/debit-cards/777"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
            .withBody(fileUtils.getFileContent("data/dcm/debit_card_manager_response.json"))));
  }

  public static void stubLMProcessWithGatewayTimeoutStatus(WireMockServer server)
      throws IOException {
    String file = "data/ledger-messages/cardauth/Cain002TransactionMessage.json";
    server.stubFor(post(urlEqualTo("/ledger-manager/v1/processPayments")).willReturn(aResponse()
        .withHeader(CONTENT_TYPE, APPLICATION_JSON))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
            .withStatus(HttpStatus.GATEWAY_TIMEOUT_504)));
  }

  public static void stubPaymentDecision(WireMockServer server, String requestBodyFile,
      String responseBodyFile) throws IOException {
    server.stubFor(post(urlEqualTo("/payment-decision/v1/payment-decision"))
        .withHeader("Content-Type", equalTo("application/json"))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
            .withBody(getFileContent(responseBodyFile))));
  }

  public static void stubPartyManagerLastAccountPeriodClosureDate(WireMockServer server)
      throws IOException {
    server.stubFor(
        get(urlEqualTo("/party-manager/tenant/party-key/10000/last-account-period-closure-date"))
            .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(fileUtils.getFileContent(PARTY_ACCOUNT_PERIOD_RESPONSE))));
  }

  public static void stubSendPfmMessage(WireMockServer server) throws IOException {
    server.stubFor(post(urlEqualTo("/queue-message-sender/send-pfm-message"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(fileUtils.getFileContent(GENERIC_RESPONSE))));
  }

  public static void stubTransactionCategorisationDetailsForASDA(WireMockServer server)
      throws IOException {
    server.stubFor(get(urlEqualTo(
        "/categorisation-service/transaction-category-details/merchant-id/ASDASUPERSTORE_04315/merchant-category-code/5411/customer-party-key/102/tenant-party-key/10000"))
        .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(fileUtils.getFileContent(CATEGORISATION_RESPONSE))));
  }

  public static void stubTransactionCategorisationDetailsForTESCO(WireMockServer server)
      throws IOException {
    server.stubFor(get(urlEqualTo(
        "/categorisation-service/transaction-category-details/merchant-id/Tesco%20Supermarkets/merchant-category-code/1098/customer-party-key/102/tenant-party-key/10000"))
        .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(fileUtils.getFileContent(CATEGORISATION_RESPONSE))));
  }

  public static void stubEventDataTransaction(WireMockServer server) throws IOException {
    server.stubFor(post(urlEqualTo("/event-data/event-data-transaction"))
        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
        .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(fileUtils.getFileContent(GENERIC_RESPONSE))));
  }

  public static void stubComputeInterestDeltaForAccrual(WireMockServer server) throws IOException {
    stubComputeInterestDelta(server, "data/response/ComputeInterestDeltaResponse.json");
  }

  public static void stubComputeInterestDeltaForApplication(WireMockServer server)
      throws IOException {
    stubComputeInterestDelta(server,
        "data/response/ComputeInterestDeltaResponseWithApplication.json");
  }

  public static void stubPdfSuccessResponse(WireMockServer server) throws IOException {
    server.stubFor(post(urlEqualTo("/payment-decision/v1/payment-decision"))
        .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(fileUtils.getFileContent(PAYMENT_DECISION_SUCCESS_RESPONSE))));
  }

  public static void stubPdfFailureResponse(WireMockServer server) throws IOException {
    server.stubFor(post(urlEqualTo("/payment-decision/v1/payment-decision"))
        .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(fileUtils.getFileContent(PAYMENT_DECISION_FAILURE_RESPONSE))));
  }

  public static void stubWorldPaySuccessResponse(WireMockServer server) throws IOException {
    server.stubFor(post(urlEqualTo("/worldpay-adapter/v1/submit-order"))
        .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(getFileContent(WORLD_PAY_ADAPTER_SUCCESS_RESPONSE))));
  }

  public static void stubWorldPayFaliureRefusedResponse(WireMockServer server) throws IOException {
    server.stubFor(post(urlEqualTo("/worldpay-adapter/v1/submit-order"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.BAD_REQUEST_400)
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(getFileContent(WORLD_PAY_ADAPTER_FAILURE_REFUSED_RESPONSE))
        ));
  }

  public static void stubFAMSuccess(WireMockServer server) throws IOException {
    server.stubFor(post(urlEqualTo("/v1/payments/submission"))
        .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(getFileContent(FAM_REQUEST_PAYAMENT_SUCCESS_RESPONSE))));
  }

  public static void stubFAMFailure(WireMockServer server) throws IOException {
    server.stubFor(post(urlEqualTo("/v1/payments/submission"))
        .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(getFileContent(FAM_REQUEST_PAYAMENT_FAILURE_RESPONSE))));
  }

  public static void stubFAM3DSecureRedirectUrl(WireMockServer server) throws IOException {
    server.stubFor(post(urlEqualTo("/v1/payments/submission"))
        .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(getFileContent(FAM_REQUEST_PAYAMENT_3D_SECURE_RESPONSE))));
  }

  private static void stubComputeInterestDelta(WireMockServer server, String file)
      throws IOException {
    server.stubFor(post(urlEqualTo("/interest/compute-interest-delta"))
        .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(fileUtils.getFileContent(file))));
  }

  public static void stubPPMSuccess(WireMockServer server) throws IOException {
    server.stubFor(post(urlEqualTo("/v1/payments/async-confirmation"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.ACCEPTED_202)));
  }
}
