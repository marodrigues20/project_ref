package com.tenx.universalbanking.transactionmanager.constants;

public class TransactionManagerExceptionCodes {

  private TransactionManagerExceptionCodes() {}

  public static final int SUBSCRIPTION_KEY_CODE_NOT_FOUND = 5200;
  public static final int INVALID_MESSAGE_CODE = 5201;
  public static final int SUBSCRIPTION_STATUS_NOT_FOUND = 5202;
  public static final int WORLDPAY_TRANSACTION_FAILURE_KEY_CODE = 5300;
  public static final int LM_POSTING_FAILED = 5301;
  public static final int NEGATIVE_AMOUNT_ERROR_CODE = 5302;
  public static final int INTERNAL_SERVICE_UNAVAILABLE = 2200;
  public static final int INTERNAL_SERVER_ERROR_PDF_FAILURE = 2201 ;
  public static final int INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE= 2202;
  public static final int INTERNAL_SERVER_ERROR_PAIN002_LM_FAILURE= 2203;
  public static final int FPSOUT_INITIATION_SERVICE_UNAVAILABLE = 2301;
  public static final int FPSOUT_SUBMISSION_SERVICE_UNAVAILABLE = 2302;
  public static final int FPSOUT_INITIATION_TIMEOUT_ERROR = 2303;
  public static final int FPSOUT_SUBMISSION_TIMEOUT_ERROR = 2304;
  //to be changed after discussion
  public static final int INTERNAL_SERVER_ERROR_NEW = 500;

  public static final int DECISION_PAYER_ACCOUNT_INACTIVE = 2306;
  public static final int DECISION_PAYMENT_LIMIT_EXCEED = 2307;
  public static final int DECISION_PAYEE_ACCOUNT_INACTIVE = 2305;
  public static final int DECISION_POOL_TRANSFER_ACCOUNT_INACTIVE = 2308;
  public static final int DECISION_CARD_AUTH_PAYMENT_LIMIT_EXCEED = 2309;
  public static final int DECISION_CARD_AUTH_ACCOUNT_INACTIVE = 2310;
  public static final int DECISION_FUND_ACCOUNT_INACTIVE = 2311;
  public static final int DECISION_FUND_LIMIT_EXCEED = 2312;
  public static final int DECISION_FPS_PAYMENT_LESS_THAN_ZERO = 2313;
  public static final int DECISION_FPS_PAYMENT_GREATER_THAN_LIMIT = 2314;
  public static final int DECISION_INSUFFICIENT_FUNDS = 2315;
}
