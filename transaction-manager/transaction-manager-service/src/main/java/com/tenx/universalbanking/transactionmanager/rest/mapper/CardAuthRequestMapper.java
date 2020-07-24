package com.tenx.universalbanking.transactionmanager.rest.mapper;

import static java.time.LocalTime.parse;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.springframework.util.StringUtils.isEmpty;

import com.tenx.universalbanking.transactionmanager.exception.RequestValidationFailureException;
import com.tenx.universalbanking.transactionmanager.model.CardAuth;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.Amounts;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.CreditDebitEnum;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.Merchant;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.Money;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.Pos;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.SchemeMessage;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.Transaction;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.TransactionRelatedDates;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CardAuthRequestMapper {

  private static final Map<Integer, List<Currency>> currencyMap =
      Currency.getAvailableCurrencies().stream()
          .collect(Collectors.groupingBy(Currency::getNumericCode, Collectors.toList()));

  public CardAuth toDomainForReversal(SchemeMessage request) {
    return buildCardAuth(request, CreditDebitEnum.CREDIT);
  }

  public CardAuth toDomain(SchemeMessage request) {
    return buildCardAuth(request, CreditDebitEnum.DEBIT);
  }

  private CardAuth buildCardAuth(SchemeMessage request, CreditDebitEnum creditDebitEnum) {

    Transaction transaction = request.getTransaction();
    Amounts amounts = transaction.getAmounts();
    Pos pos = request.getPos();
    Merchant merchant = request.getMerchant();
    TransactionRelatedDates transactionRelatedDates = request.getTransaction()
        .getTransactionRelatedDates();
    try {
      Money billingAmount = amounts.getBilling();

      if (isEmpty(billingAmount.getCurrency())) {
        throw new RequestValidationFailureException(
            "transaction currency code is empty and requires a value");
      }

      return CardAuth.builder()
          .cardId(request.getCard().getId())
          .totalAmount(billingAmount.getAmount())
          .amount(amounts.getTransaction().getAmount())
          .transactionType(request.getProcessingCode())
          .transactionCurrencyCode(
              getCurrencyByIsoCode(Integer.valueOf(billingAmount.getCurrency())))
          .merchantCategoryCode(merchant.getCategoryCode())
          .merchantCountryCode(getMerchantCountryCode(merchant))
          .merchantName(merchant.getName())
          .cardDataEntryMode(request.getPos().getPosEntryMode())
          .transactionDatetime(getTransactionDatetime(transactionRelatedDates))
          .transactionDate(transactionRelatedDates.getTransactionDate())
          .transactionTime(transactionRelatedDates.getTransactionTime())
          .exchangeRate(amounts.getConversionRate())
          .banknetReference(transaction.getBanknetReferenceNumber())
          .creditDebit(creditDebitEnum)
          .cardConditionCode(pos.getConditionCode())
          .feeAmount(getFee(amounts.getFee()))
          .systemTraceNumber(request.getSystemTraceNumber())
          .networkCode(transaction.getNetworkCode())
          .cardAcceptorIdCode(merchant.getAcceptorIdCode())
          .conversionRate(amounts.getConversionRate())
          .build();
    } catch (NullPointerException ex) {
      log.error("CardAuth request received is missing a required field", ex);
      throw new RequestValidationFailureException(ex.getMessage());
    }
  }

  private BigDecimal getFee(Money money) {
    return money != null ? money.getAmount() : null;
  }

  private Instant getTransactionDatetime(TransactionRelatedDates transactionRelatedDates) {
    return OffsetDateTime.of(transactionRelatedDates.getTransactionDate(),
        parse(transactionRelatedDates.getTransactionTime(), ofPattern("HHmmss")), UTC)
        .toInstant();
  }

  private String getMerchantCountryCode(Merchant merchant) {
    return merchant.getAddress() != null ? merchant.getAddress().getStateOrCountryCode() : null;
  }

  private String getCurrencyByIsoCode(Integer isoCode) {
    return currencyMap.get(isoCode).get(0).getCurrencyCode();
  }

}
