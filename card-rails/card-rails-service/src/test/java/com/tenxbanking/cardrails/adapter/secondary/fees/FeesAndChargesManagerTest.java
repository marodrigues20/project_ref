package com.tenxbanking.cardrails.adapter.secondary.fees;

import static com.tenxbanking.cardrails.domain.TestConstant.CAIN_001;
import static com.tenxbanking.cardrails.domain.TestConstant.CAIN_003;
import static com.tenxbanking.cardrails.domain.TestConstant.CORRELATION_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.SUBSCRIPTION_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TRANSACTION_ID;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.adapter.primary.rest.mapper.TransactionCodeMapper;
import com.tenxbanking.cardrails.adapter.secondary.fees.model.FeeResponse;
import com.tenxbanking.cardrails.adapter.secondary.fees.model.FeeTransactionRequest;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Fee;
import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.service.TimeService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class FeesAndChargesManagerTest {

  private static final Instant INSTANT = Instant.parse("2019-03-18T10:12:14.156Z");
  private static final String INSTANT_ISO_8601 = "2019-03-18T10:12:14.156+0000";

  @Mock
  private FeesAndChargesClient feesAndChargesClient;

  @Mock
  private TransactionCodeMapper codeMapper;

  @Mock
  private TimeService timeService;

  @InjectMocks
  private FeesAndChargesManager unit;

  @Test
  void shouldCallFeesClientAndReturnAFeeWhenSubscriptionSettingsHasFees() {

    when(codeMapper
        .getTransactionCode(CAIN_001.getPaymentMethodType()))
        .thenReturn("transactionCode");
    when(timeService.fromInstant(CAIN_001.getTransactionDate())).thenReturn(INSTANT_ISO_8601);

    when(feesAndChargesClient.postTransaction(any(FeeTransactionRequest.class)))
        .thenReturn(ResponseEntity.ok().build());
    when(codeMapper
        .getTransactionCode(CAIN_001.getPaymentMethodType()))
        .thenReturn("transactionCode");

    unit.check(CAIN_001, SUBSCRIPTION_KEY);

    ArgumentCaptor<FeeTransactionRequest> argumentCaptor = ArgumentCaptor
        .forClass(FeeTransactionRequest.class);

    verify(feesAndChargesClient).postTransaction(argumentCaptor.capture());

    FeeTransactionRequest request = argumentCaptor.getValue();
    assertThat(request.getAmountQualifier()).isEqualTo("accountQual");
    assertThat(request.getCurrency()).isEqualTo("GBP");
    assertThat(request.getSubscriptionKey()).isEqualTo(SUBSCRIPTION_KEY.toString());
    assertThat(request.getMerchantCategoryCode()).isEqualTo("merchantCatCode");
    assertThat(request.getTransactionType()).isEqualTo("processingCode");
    assertThat(request.getTransactionId()).isEqualTo(TRANSACTION_ID.toString());
    assertThat(request.getTransactionCorrelationId()).isEqualTo(CORRELATION_ID.toString());
    assertThat(request.getTransactionAmount()).isEqualTo(BigDecimal.TEN);
    assertThat(request.getTransactionCode()).isEqualTo("transactionCode");
    assertThat(request.getTransactionDate()).isEqualTo(INSTANT_ISO_8601);
  }

  @Test
  void shouldReturnOptionalEmptyWhenResponseIsNotCreated() {

    when(feesAndChargesClient.postTransaction(any(FeeTransactionRequest.class)))
        .thenReturn(ResponseEntity.ok().build());
    when(codeMapper
        .getTransactionCode(CAIN_001.getPaymentMethodType()))
        .thenReturn("transactionCode");

    Optional<Fee> fee = unit.check(CAIN_001, SUBSCRIPTION_KEY);

    assertThat(fee).isEmpty();
  }

  @Test
  void shouldReturnFeeResponseWhenCreated() {

    when(feesAndChargesClient.postTransaction(any(FeeTransactionRequest.class)))
        .thenReturn(createResponse());
    when(codeMapper
        .getTransactionCode(CAIN_001.getPaymentMethodType()))
        .thenReturn("transactionCode");

    Optional<Fee> feeOptional = unit.check(CAIN_001, SUBSCRIPTION_KEY);
    assertThat(feeOptional).isPresent();

    Fee fee = feeOptional.get();
    assertThat(fee.getAmount()).isEqualTo(BigDecimal.ONE);
    assertThat(fee.getFeeCurrencyCode()).isEqualTo("GBP");
    assertThat(fee.getStatus()).isEqualTo("posted");
    assertThat(fee.getTax().get().getTaxAmount()).isEqualTo(BigDecimal.ONE);
    assertThat(fee.getTransactionCode()).isEqualTo("FEE");
    assertThat(fee.getTransactionCorrelationId()).isEqualTo("789");
    assertThat(fee.getTransactionDate()).isEqualTo("2019-03-01T10:28:58.460+0000");
    assertThat(fee.getTransactionId()).isEqualTo("001");
    assertThat(fee.getValueDateTime()).isEqualTo("2019-03-02T10:28:58.460+0000");
  }



  // == NEW

  @Test
  void shouldCallFeesClientAndReturnAFeeWhenSubscriptionSettingsHasFeesToCain003() {

    when(codeMapper
        .getTransactionCode(CAIN_003.getPaymentMethodType()))
        .thenReturn("transactionCode");
    when(timeService.fromInstant(CAIN_003.getTransactionDate())).thenReturn(INSTANT_ISO_8601);

    when(feesAndChargesClient.postTransaction(any(FeeTransactionRequest.class)))
        .thenReturn(ResponseEntity.ok().build());
    when(codeMapper
        .getTransactionCode(CAIN_003.getPaymentMethodType()))
        .thenReturn("transactionCode");

    unit.check(CAIN_003, SUBSCRIPTION_KEY);

    ArgumentCaptor<FeeTransactionRequest> argumentCaptor = ArgumentCaptor
        .forClass(FeeTransactionRequest.class);

    verify(feesAndChargesClient).postTransaction(argumentCaptor.capture());

    FeeTransactionRequest request = argumentCaptor.getValue();
    assertThat(request.getAmountQualifier()).isEqualTo("accountQual");
    assertThat(request.getCurrency()).isEqualTo("GBP");
    assertThat(request.getSubscriptionKey()).isEqualTo(SUBSCRIPTION_KEY.toString());
    assertThat(request.getMerchantCategoryCode()).isEqualTo("merchantCatCode");
    assertThat(request.getTransactionType()).isEqualTo("processingCode");
    assertThat(request.getTransactionId()).isEqualTo(TRANSACTION_ID.toString());
    assertThat(request.getTransactionCorrelationId()).isEqualTo(CORRELATION_ID.toString());
    assertThat(request.getTransactionAmount()).isEqualTo(BigDecimal.TEN);
    assertThat(request.getTransactionCode()).isEqualTo("transactionCode");
    assertThat(request.getTransactionDate()).isEqualTo(INSTANT_ISO_8601);
  }

  @Test
  void shouldReturnOptionalEmptyWhenResponseIsNotCreatedToCain003() {

    when(feesAndChargesClient.postTransaction(any(FeeTransactionRequest.class)))
        .thenReturn(ResponseEntity.ok().build());
    when(codeMapper
        .getTransactionCode(CAIN_003.getPaymentMethodType()))
        .thenReturn("transactionCode");

    Optional<Fee> fee = unit.check(CAIN_003, SUBSCRIPTION_KEY);

    assertThat(fee).isEmpty();
  }


  @Test
  void shouldReturnFeeResponseWhenCreatedToCain003() {

    when(feesAndChargesClient.postTransaction(any(FeeTransactionRequest.class)))
        .thenReturn(createResponse());
    when(codeMapper
        .getTransactionCode(CAIN_003.getPaymentMethodType()))
        .thenReturn("transactionCode");

    Optional<Fee> feeOptional = unit.check(CAIN_003, SUBSCRIPTION_KEY);
    assertThat(feeOptional).isPresent();

    Fee fee = feeOptional.get();
    assertThat(fee.getAmount()).isEqualTo(BigDecimal.ONE);
    assertThat(fee.getFeeCurrencyCode()).isEqualTo("GBP");
    assertThat(fee.getStatus()).isEqualTo("posted");
    assertThat(fee.getTax().get().getTaxAmount()).isEqualTo(BigDecimal.ONE);
    assertThat(fee.getTransactionCode()).isEqualTo("FEE");
    assertThat(fee.getTransactionCorrelationId()).isEqualTo("789");
    assertThat(fee.getTransactionDate()).isEqualTo("2019-03-01T10:28:58.460+0000");
    assertThat(fee.getTransactionId()).isEqualTo("001");
    assertThat(fee.getValueDateTime()).isEqualTo("2019-03-02T10:28:58.460+0000");
  }

  private ResponseEntity<FeeResponse> createResponse() {
    return ResponseEntity.status(HttpStatus.CREATED).body(
        FeeResponse.builder()
            .amount(BigDecimal.ONE)
            .description("description")
            .feeCurrencyCode("GBP")
            .parentTransactionId("123")
            .status("posted")
            .subscriptionKey("456")
            .taxAmount(BigDecimal.ONE)
            .transactionCode("FEE")
            .transactionCorrelationId("789")
            .transactionDate("2019-03-01T10:28:58.460+0000")
            .transactionId("001")
            .valueDateTime("2019-03-02T10:28:58.460+0000").build());
  }

}