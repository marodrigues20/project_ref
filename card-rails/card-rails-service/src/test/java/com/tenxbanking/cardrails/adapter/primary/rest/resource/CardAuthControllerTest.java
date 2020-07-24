package com.tenxbanking.cardrails.adapter.primary.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenxbanking.cardrails.adapter.primary.rest.exception.HttpExceptionHandler;
import com.tenxbanking.cardrails.adapter.primary.rest.exception.RequestValidationFailureException;
import com.tenxbanking.cardrails.adapter.primary.rest.mapper.Cain001Mapper;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.SchemeMessage;
import com.tenxbanking.cardrails.adapter.secondary.redis.RedisIdempotentRepository;
import com.tenxbanking.cardrails.domain.exception.CardAuthHandlerUnsupportedException;
import com.tenxbanking.cardrails.domain.exception.CardAuthReservationException;
import com.tenxbanking.cardrails.domain.exception.CardNotFoundException;
import com.tenxbanking.cardrails.domain.exception.LimitConstraintException;
import com.tenxbanking.cardrails.domain.exception.SubscriptionNotFoundException;
import com.tenxbanking.cardrails.domain.exception.ValidationException;
import com.tenxbanking.cardrails.domain.model.AuthResponseCode;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.service.handler.CardAdviceHandler;
import com.tenxbanking.cardrails.domain.service.handler.CardAuthHandler;
import com.tenxbanking.cardrails.domain.service.handler.CardAuthReversalHandler;
import com.tenxbanking.cardrails.domain.validator.RequestValidator;
import com.tenxbanking.cardrails.domain.validator.ValidationFailure;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CardAuthControllerTest {

  @Mock
  private CardAuthHandler cardAuthHandler;
  @Mock
  private CardAdviceHandler cardAdviceHandler;
  @Mock
  private CardAuthReversalHandler cardAuthReversalHandler;
  @Mock
  private Cain001Mapper cain001Mapper;
  @Mock
  private RequestValidator reversalsValidator;

  @InjectMocks
  private CardAuthController unit;

  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    this.mockMvc = MockMvcBuilders.standaloneSetup(unit)
        .setControllerAdvice(new HttpExceptionHandler(mock(RedisIdempotentRepository.class)))
        .build();
  }

  @Test
  void shouldCallCardAuthHandlerForAuthEndpoint() throws Exception {

    Cain001 cain001 = mock(Cain001.class);
    Cain002 cain002 = new Cain002("123456", Money.of(BigDecimal.ONE, "EUR"), AuthResponseCode._00,
        true);

    when(cain001Mapper.toDomain(any(SchemeMessage.class), any())).thenReturn(cain001);
    when(cardAuthHandler.auth(cain001)).thenReturn(cain002);

    mockMvc.perform(post("/api/v1/transactions/authorisation")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authCode").isNotEmpty())
        .andExpect(jsonPath("$.updatedBalance").value(notNullValue()))
        .andExpect(jsonPath("$.reasonCode").value("00"));

    verify(cardAuthHandler).auth(cain001);
    verifyZeroInteractions(cardAdviceHandler, cardAuthReversalHandler);
  }

  @Test
  void shouldCallCardAdviceHandlerForAdviceEndpoint() throws Exception {
    Cain001 cain001 = mock(Cain001.class);
    Cain002 cain002 = new Cain002("123456", Money.of(BigDecimal.ONE, "EUR"), AuthResponseCode._00,
        true);

    when(cain001Mapper.toDomain(any(SchemeMessage.class), any())).thenReturn(cain001);
    when(cardAdviceHandler.auth(cain001)).thenReturn(cain002);

    mockMvc.perform(post("/api/v1/transactions/authorisation_advice")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authCode").isNotEmpty())
        .andExpect(jsonPath("$.updatedBalance").value(notNullValue()))
        .andExpect(jsonPath("$.reasonCode").value("00"));

    verify(cardAdviceHandler).auth(cain001);
    verifyZeroInteractions(cardAuthHandler, cardAuthReversalHandler);
  }

  @Test
  void shouldCallCardReversalHandlerForReversalEndpoint() throws Exception {
    Cain001 cain001 = mock(Cain001.class);
    Cain002 cain002 = new Cain002("123456", Money.of(BigDecimal.ONE, "EUR"), AuthResponseCode._00,
        true);

    when(cain001Mapper.toDomain(any(SchemeMessage.class), eq(CardTransactionType.REVERSAL)))
        .thenReturn(cain001);
    when(cardAuthReversalHandler.auth(cain001)).thenReturn(cain002);

    mockMvc.perform(post("/api/v1/transactions/reversal_advice")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authCode").isNotEmpty())
        .andExpect(jsonPath("$.updatedBalance").value(notNullValue()))
        .andExpect(jsonPath("$.reasonCode").value("00"));

    verify(cardAuthReversalHandler).auth(cain001);
    verifyZeroInteractions(cardAdviceHandler, cardAdviceHandler);

    ArgumentCaptor<SchemeMessage> schemeMessageArgumentCaptor = ArgumentCaptor
        .forClass(SchemeMessage.class);
    verify(cain001Mapper)
        .toDomain(schemeMessageArgumentCaptor.capture(), eq(CardTransactionType.REVERSAL));
    verify(reversalsValidator).validate(schemeMessageArgumentCaptor.capture());

    List<SchemeMessage> schemeMessages = schemeMessageArgumentCaptor.getAllValues();

    assertThat(schemeMessages.size()).isEqualTo(2);
    assertThat(schemeMessages.get(0)).isEqualTo(schemeMessages.get(1));
  }

  @Test
  void shouldReturnBadGateway() throws Exception {
    Cain001 cain001 = mock(Cain001.class);

    when(cain001Mapper.toDomain(any(SchemeMessage.class), any())).thenReturn(cain001);
    when(cardAuthHandler.auth(cain001))
        .thenThrow(new CardAuthReservationException("Ledger Not available"));

    mockMvc.perform(post("/api/v1/transactions/authorisation")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadGateway())
        .andExpect(content().json(CARD_AUTH_ERROR, true));
  }

  @Test
  void shouldReturnInlineResponse200WhenLimitsAreExceeded() throws Exception {
    Cain001 cain001 = mock(Cain001.class);

    when(cain001Mapper.toDomain(any(SchemeMessage.class), any())).thenReturn(cain001);
    doThrow(LimitConstraintException.class).when(cardAuthHandler).auth(cain001);

    mockMvc.perform(post("/api/v1/transactions/authorisation")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authCode").value(nullValue()))
        .andExpect(jsonPath("$.updatedBalance").value(nullValue()))
        .andExpect(jsonPath("$.reasonCode").value("05"));
  }

  @Test
  void shouldReturnBadRequest() throws Exception {
    when(cain001Mapper.toDomain(any(SchemeMessage.class), any()))
        .thenThrow(new RequestValidationFailureException("Request contains null"));

    mockMvc.perform(post("/api/v1/transactions/authorisation")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().json("{\"message\":\"Request contains null\",\"code\":null}", true));
  }

  @Test
  void returns422WhenCardNotFound() throws Exception {
    when(cain001Mapper.toDomain(any(SchemeMessage.class), any()))
        .thenThrow(new CardNotFoundException());

    mockMvc.perform(post("/api/v1/transactions/authorisation")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("Card cannot be found"));

  }

  @Test
  void returns422WhenSubscriptionNotFound() throws Exception {
    when(cain001Mapper.toDomain(any(SchemeMessage.class), any()))
        .thenThrow(new SubscriptionNotFoundException());

    mockMvc.perform(post("/api/v1/transactions/authorisation")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("Subscription cannot be found"));
  }

  @Test
  void returns422WhenValidationException() throws Exception {
    when(cain001Mapper.toDomain(any(SchemeMessage.class), any()))
        .thenThrow(new ValidationException(
            List.of(ValidationFailure.of("a failure"), ValidationFailure.of("another failure"))));

    mockMvc.perform(post("/api/v1/transactions/authorisation")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("[a failure, another failure]"));
  }

  @Test
  void shouldReturnUnprocessableEntity() throws Exception {
    Cain001 cain001 = mock(Cain001.class);

    when(cain001Mapper.toDomain(any(SchemeMessage.class), any())).thenReturn(cain001);
    when(cardAuthHandler.auth(cain001))
        .thenThrow(new CardAuthHandlerUnsupportedException("Unsupported Message"));

    mockMvc.perform(post("/api/v1/transactions/authorisation")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().json("{\"message\":\"Unsupported Message\",\"code\":null}", true));
  }

  private static final String CARD_AUTH_REQUEST =
      "{\n"
          + "  \"messageType\": \"Authorisation\",\n"
          + "  \"processingCode\": \"000000\",\n"
          + "  \"systemTraceNumber\": \"113059\",\n"
          + "  \"transaction\": {\n"
          + "    \"amounts\": {\n"
          + "      \"transaction\": {\n"
          + "        \"amount\": 1,\n"
          + "        \"currency\": \"826\"\n"
          + "      },\n"
          + "      \"settlement\": {},\n"
          + "      \"billing\": {\n"
          + "        \"amount\": 1,\n"
          + "        \"currency\": \"826\"\n"
          + "      },\n"
          + "      \"fee\": {},\n"
          + "      \"additionalAmounts\": [],\n"
          + "      \"conversionRate\": 2.032479\n"
          + "    },\n"
          + "    \"networkCode\": \"MCC\",\n"
          + "    \"banknetReferenceNumber\": \"00001106T\",\n"
          + "    \"dates\": {\n"
          + "      \"transactionDate\": \"2019-10-09\",\n"
          + "      \"transactionTime\": \"094022\",\n"
          + "      \"settlementDate\": \"2019-10-05\",\n"
          + "      \"conversionDate\": \"2019-10-05\",\n"
          + "      \"transmissionDateTime\": \"2019-05-10T09:46:13Z\"\n"
          + "    },\n"
          + "    \"retrievalReferenceNumber\": null\n"
          + "  },\n"
          + "  \"card\": {\n"
          + "    \"id\": \"435353856509933\",\n"
          + "    \"expiryDate\":\"0922\"\n"
          + "  },\n"
          + "  \"acquirer\": {\n"
          + "    \"id\": \"012825\"\n"
          + "  },\n"
          + "  \"merchant\": {\n"
          + "    \"name\": \"MPTS ECOMM\",\n"
          + "    \"address\": {\n"
          + "      \"cityName\": \"WARSZAWA\",\n"
          + "       \"countryCode\": \"GBR\"\n"
          + "    },\n"
          + " \n"
          + "    \"terminalId\": \"00018594\",\n"
          + "    \"categoryCode\": \"5045\",\n"
          + "    \"acceptorIdCode\": \"0001859412     \"\n"
          + "  },\n"
          + "  \"pos\": {\n"
          + "    \"additionalPosDetail\": \"1025100006000616902101234\",\n"
          + "    \"posEntryMode\": \"051\",\n"
          + "    \"conditionCode\":\"00\"\n"
          + "  },\n"
          + "  \"additionalData\": {\n"
          + "    \"eCommerceIndicator\": \"210\",\n"
          + "    \"onBehalfOfServices\": \"18C \"\n"
          + "  },\n"
          + "  \"fraudScoreData\": {\n"
          + "    \"rawData\": \"01038250202XX\"\n"
          + "  },\n"
          + "  \"authCode\": null,\n"
          + "  \"authResponseCode\": null,\n"
          + "  \"reversalAmounts\": {}\n"
          + "}";

  private static final String CARD_AUTH_ERROR = "{\"message\":\"Ledger Not available\",\"code\":null}";


}