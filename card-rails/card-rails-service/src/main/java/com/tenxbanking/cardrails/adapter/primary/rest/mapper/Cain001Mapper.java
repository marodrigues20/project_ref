package com.tenxbanking.cardrails.adapter.primary.rest.mapper;

import static java.time.LocalTime.parse;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ofPattern;

import com.tenxbanking.cardrails.adapter.primary.rest.exception.RequestValidationFailureException;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.Amounts;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.Merchant;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.SchemeMessage;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.Transaction;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.TransactionRelatedDates;
import com.tenxbanking.cardrails.domain.model.AuthResponseCode;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.Money;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class Cain001Mapper {

  private static final String ACCOUNT_QUALIFIER = "ACTL";
  private static DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("HH:mm:ss.SSS");

  private final PaymentMethodTypeMapper paymentMethodTypeMapper;

  public Cain001 toDomain(@NonNull final SchemeMessage schemeMessage, CardTransactionType cardTransactionType) {

    try {
      Transaction transaction = schemeMessage.getTransaction();
      Merchant merchant = schemeMessage.getMerchant();
      Amounts amounts = transaction.getAmounts();

      boolean validReversal = schemeMessage.getReversalAmounts() != null
          //&& schemeMessage.getReversalAmounts().getTransaction() != null
          && schemeMessage.getReversalAmounts().getBilling() != null;

      return Cain001.builder()
          .transactionAmount(amounts.getTransaction() == null ? null : amounts.getTransaction().toDomain())
          .billingAmount(amounts.getBilling().toDomain())
          .settlementAmount(amounts.getSettlement() == null ? null : amounts.getSettlement().toDomain())
          .merchantCategoryCode(merchant.getCategoryCode())
          .transactionDate(getTransactionDate(transaction.getTransactionRelatedDates()))
          .accountQualifier(ACCOUNT_QUALIFIER)
          .cardId(schemeMessage.getCard().getId())
          .processingCode(schemeMessage.getProcessingCode())
          .conversionRate(amounts.getConversionRate())
          .cardExpiryDate(schemeMessage.getCard().getExpiryDate())
          .pointOfServiceEntryMode(schemeMessage.getPos().getPosEntryMode())
          .pointOfServiceConditionCode(schemeMessage.getPos().getConditionCode())
          .networkId(transaction.getNetworkCode())
          .cardAcceptorCountryCode(merchant.getAddress().getStateOrCountryCode())
          .banknetReferenceNumber(transaction.getBanknetReferenceNumber())
          .cardTransactionType(cardTransactionType)
          .retrievalReferenceNumber(transaction.getRetrievalReferenceNumber())
          .authCode(schemeMessage.getAuthCode())
          .authResponseCode(schemeMessage.getAuthResponseCode() == null ? null : AuthResponseCode.getByValue(schemeMessage.getAuthResponseCode()))
          .paymentMethodType(paymentMethodTypeMapper.map(
              schemeMessage.getProcessingCode(),
              schemeMessage.getMerchant().getAddress().getStateOrCountryCode(),
              schemeMessage.getPos().getPosEntryMode(),
              schemeMessage.getPos().getExtendedDataConditionCodes()))
          .reversalAmount(
              (validReversal) ? schemeMessage.getReversalAmounts().toDomain(amounts) : null
          ).build();
    } catch (NullPointerException npe) {
      log.info("Caught NullPointerException and rethrowing as validation error ", npe);
      throw new RequestValidationFailureException(npe.getMessage());
    }
  }

  private Instant getTransactionDate(TransactionRelatedDates transactionRelatedDates) {
    return OffsetDateTime.of(transactionRelatedDates.getTransactionDate(),
        parse(transactionRelatedDates.getTransactionTime(), DATE_TIME_FORMATTER), UTC)
        .toInstant();
  }

}
