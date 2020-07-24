package com.tenx.universalbanking.transactionmanager.componenttest.utils;

public class Constants {

  public static final Integer WIREMOCK_PORT = 28082;
  public static final String PAYMENT_PROXY_COMMAND_TOPIC_NAME = "payment-proxy-commands-topic";
  public static final String PAYMENT_PROXY_EVENT_TOPIC = "payment-proxy-events-topic";
  public static final String TRANSACTION_MANAGER_COMMAND_TOPIC_NAME = "transaction-manager-commands-topic";
  public static final String TRANSACTION_MANAGER_EVENT_TOPIC = "transaction-manager-events-topic";
  public static final String PAYMENT_COMMAND_TOPIC_NAME = "payment-commands-topic";
  public static final String PAYMENT_EVENT_TOPIC = "payment-events-topic";

  public static final String TRANSACTION_ENRICHMENT_COMMAND_TOPIC = "transaction-enrichment-commands-topic";
  public static final String FEES_AND_CHARGES_COMMAND_TOPIC = "fees-charges-commands-topic";
  public static final String INTEREST_COMMAND_TOPIC_NAME = "transaction-interest-commands-topic";
  public static final String FEES_AND_CHARGES_EVENTS_TOPIC = "fees-charges-events-topic";
  public static final String LEDGER_COMMAND_TOPIC_NAME = "ledger-manager-commands-topic";
  public static final String LEDGER_PAYMENT_MESSAGE_TOPIC_NAME = "payment-messages-topic";

  public static final Boolean IGNORE_ARRAY_ORDER = true;
  public static final Boolean IGNORE_EXTRA_ELEMENTS = true;

  public static final String CONTENT_TYPE = "Content-Type";
  public static final String APPLICATION_JSON = "application/json";
}
