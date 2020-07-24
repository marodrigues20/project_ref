package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.utils.LoggerUtils.appendTransactionMessageRequest;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.BANKNET_REFERENCE_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TOTAL_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TRANSACTION_DATE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.AUTHORISATION_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.DebitCreditIndicatorEnum.CREDIT;
import static com.tenx.universalbanking.transactionmessage.enums.DebitCreditIndicatorEnum.DEBIT;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.CARD_PROCESSOR_ACCOUNT_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.DEBIT_CREDIT_INDICATOR;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PAYMENT_METHOD_TYPE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_ID;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN002;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_CARD_TRANSACTION_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_CARD_TRANSACTION_CROSS_BORDER_CASH_WITHDRAWEL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_CARD_TRANSACTION_POINT_OF_SALE_PAYMENT_DEBIT_CARD;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_MAG_STRIPE_TRANSACTION_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.UNKNOWN;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.universalbanking.transactionmanager.entity.AuthorisationId;
import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.repository.PaymentAuthorisations;
import com.tenx.universalbanking.transactionmanager.service.helpers.AuthorisationFinder;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.DebitCreditIndicatorEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

abstract class AbstractCardAuthMessageService {

  private final Logger logger = getLogger(AbstractCardAuthMessageService.class);

  @Autowired
  private PaymentAuthorisations paymentAuthorisations;

  @Autowired
  private AuthorisationFinder authorisationFinder;

  private static final String AUTH = "AUTH";
  private static final String REVERSAL = "REVERSAL";

  private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  public void generateTransactionCode(TransactionMessage cardAuthMessage) {
    cardAuthMessage.getMessages().forEach(this::setTransactionCode);
  }

  private void setTransactionCode(PaymentMessage paymentMessage) {

    PaymentMethodTypeEnum paymentMethodType =
        convertToEnum(
            paymentMessage.getAdditionalInfo().get(PAYMENT_METHOD_TYPE.name()).toString());

    switch (paymentMethodType) {
      case DOMESTIC_CASH_WITHDRAWAL:
        setTransactionCode(paymentMessage, PAYMENTS_CUSTOMER_CARD_TRANSACTION_CASH_WITHDRAWAL);
        break;

      case INTERNATIONAL_CASH_WITHDRAWAL:
        setTransactionCode(paymentMessage,
            PAYMENTS_CUSTOMER_CARD_TRANSACTION_CROSS_BORDER_CASH_WITHDRAWEL);
        break;

      case DOMESTIC_POS_CHIP_AND_PIN:
        setTransactionCode(paymentMessage,
            PAYMENTS_CUSTOMER_CARD_TRANSACTION_POINT_OF_SALE_PAYMENT_DEBIT_CARD);
        break;

      case POS_MAG_STRIPE:
        setTransactionCode(paymentMessage,
            PAYMENTS_CUSTOMER_MAG_STRIPE_TRANSACTION_CASH_WITHDRAWAL);
        break;

      default:
        setTransactionCode(paymentMessage, UNKNOWN);
    }
  }

  private PaymentMethodTypeEnum convertToEnum(String value) {
    return PaymentMethodTypeEnum.valueOf(value);
  }

  private void setTransactionCode(PaymentMessage item, PaymentsTransactionCodeEnum codeEnum) {
    Map<String, Object> additionalInfo = item.getAdditionalInfo();

    additionalInfo.put(TRANSACTION_CODE.name(), codeEnum.getValue());

    logger.debug("Setting TransactionCode Value: {}",
        additionalInfo.get(TRANSACTION_CODE.name()));
  }

  void saveCardAuthMessage(TransactionMessage cain002Response, boolean isReversal) {
    List<Authorisations> authList = new ArrayList<>();
    for (PaymentMessage message : cain002Response.getMessages()) {
      if (CAIN002.name().equals(message.getType())) {
        Map<String, Object> map = message.getMessage();
        Authorisations auth = new Authorisations();
        AuthorisationId authid = new AuthorisationId();

        try {
          authid.setTransactionDate(
              ZonedDateTime.parse(map.get(TRANSACTION_DATE.name()).toString(), dateTimeFormatter)
                  .toLocalDate());
        } catch (DateTimeParseException e) {
          logger.error("Exception in parsing Datetime value: {}", e);
          logger.error("Input Transaction Message {} ",
              appendTransactionMessageRequest(cain002Response));
          Instant instant = Instant.parse(map.get(TRANSACTION_DATE.name()).toString());
          LocalDateTime result = LocalDateTime
              .ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
          authid.setTransactionDate(result.toLocalDate());
        }
        //TO DO GET THE TSYS ACCOUNT ID and BANKNET_REFERENCE_NUMBER WHEN THE STORY IS PLAYED
        authid.setAuthorisationCode(map.get(AUTHORISATION_CODE.name()).toString());
        authid.setBankNetReferenceNumber(
            Optional.ofNullable((String) map.get(BANKNET_REFERENCE_NUMBER.name())).orElse(""));

        auth.setId(authid);
        auth.setMcc(map.get(MERCHANT_CATEGORY_CODE.name()).toString());
        auth.setTransactionId(message.getAdditionalInfo().get(TRANSACTION_ID.name()).toString());
        auth.setCorrelationId(
            cain002Response.getAdditionalInfo().get(TRANSACTION_CORRELATION_ID.name()).toString());
        auth.setTsysAccountId(
            Optional.ofNullable((String) map.get(CARD_PROCESSOR_ACCOUNT_ID.name())).orElse(""));
        auth.setMatched(isReversal);
        auth.setExpired(false);
        auth.setTransactionType(isReversal ? REVERSAL : AUTH);
        BigDecimal transactionAmountBD = new BigDecimal(map.get(TOTAL_AMOUNT.name()).toString());
        auth.setTransactionAmount(transactionAmountBD);
        authList.add(auth);
      }
    }
    paymentAuthorisations.saveAll(authList);
  }

  void updateAuthStatusWhileReversal(Authorisations auth) {
    auth.setMatched(true);
    paymentAuthorisations.save(auth);
  }

  boolean isReversal(TransactionMessage cardAuthMessage) {
    Object debitCreditIndicatorObject = cardAuthMessage.getMessages().stream().findFirst()
        .map(PaymentMessage::getAdditionalInfo).get()
        .get(DEBIT_CREDIT_INDICATOR.name());

    String debitCreditIndicator = DEBIT.name();
    // It can either be String/DebitCreditIndicatorEnum object. Any other object will be a error case and
    // transaction to be defaulted to DEBIT.
    if (debitCreditIndicatorObject instanceof String) {
      debitCreditIndicator = debitCreditIndicatorObject.toString();
    } else if (debitCreditIndicatorObject instanceof DebitCreditIndicatorEnum) {
      debitCreditIndicator = ((DebitCreditIndicatorEnum) debitCreditIndicatorObject).name();
    }

    return CREDIT.name().equalsIgnoreCase(debitCreditIndicator);
  }

  Authorisations getMatchingAuth(TransactionMessage cardAuthMessage, boolean isReversal,
      boolean authForReversal) {
    PaymentMessage paymentMessage = cardAuthMessage.getMessages().get(0);
    Map<String, Object> messageMap = paymentMessage.getMessage();
    String tsysAccountId = String
        .valueOf(paymentMessage.getAdditionalInfo().get(CARD_PROCESSOR_ACCOUNT_ID.name()));
    LocalDate transactionDate = ZonedDateTime
        .parse(messageMap.get(TRANSACTION_DATE.name()).toString(), dateTimeFormatter).toLocalDate();
    BigDecimal transactionAmount = new BigDecimal(messageMap.get(TOTAL_AMOUNT.name()).toString());
    String bankNetReferenceNumber = (String) messageMap.get(BANKNET_REFERENCE_NUMBER.name());
    Authorisations matchingAuth;
    if (authForReversal) {
      // We need matching auth entry for reversal as we need to pick up transaction id/correlation id from auth
      matchingAuth = authorisationFinder.
          getAuthorisationEntryForReversal(tsysAccountId, bankNetReferenceNumber);
    } else {
      // This section will find matching auth for provided condition for both auth/reversal.
      // This entry will decide if its a duplicate request
      Optional<Authorisations> authorisations = authorisationFinder.getAuthorisation(tsysAccountId,
          transactionDate, transactionAmount, bankNetReferenceNumber, isReversal);

      matchingAuth = authorisations.orElse(null);
    }
    if (matchingAuth != null) {
      logger.debug("Existing transaction. BankNetReferenceNumber: {} ", bankNetReferenceNumber);
    }
    return matchingAuth;
  }

}
