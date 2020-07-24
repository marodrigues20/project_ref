package com.tenx.universalbanking.transactionmanager.rest.controllers;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tenx.universalbanking.transactionmanager.exception.CardAuthProxyExceptionHandler;
import com.tenx.universalbanking.transactionmanager.exception.CardNotFoundException;
import com.tenx.universalbanking.transactionmanager.exception.RequestValidationFailureException;
import com.tenx.universalbanking.transactionmanager.model.CardAuth;
import com.tenx.universalbanking.transactionmanager.rest.mapper.CardAuthRequestMapper;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.Money;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.SchemeMessage;
import com.tenx.universalbanking.transactionmanager.rest.responses.turbine.SchemeMessageResponse;
import com.tenx.universalbanking.transactionmanager.service.turbine.TurbineAuthService;
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
public class TurbineCardAuthProxyControllerTest {

  @Mock
  private TurbineAuthService processor;
  @Mock
  private CardAuthRequestMapper mapper;

  @InjectMocks
  private TurbineCardAuthProxyController unit;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.standaloneSetup(unit)
        .setControllerAdvice(new CardAuthProxyExceptionHandler())
        .setMessageConverters(jacksonDateTimeConverter())
        .build();
  }

  @Test
  public void shouldReturnResponseFromServiceForAuth() throws Exception {
    CardAuth cardAuth = mock(CardAuth.class);
    SchemeMessageResponse response = createResponse();

    when(mapper.toDomain(getSchemeMessage())).thenReturn(cardAuth);
    when(processor.authorise(cardAuth)).thenReturn(response);

    mockMvc.perform(post("/v1/scheme-message/authorisation")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.authCode").value(1234))
        .andExpect(jsonPath("$.updatedBalance.amount").value("1"))
        .andExpect(jsonPath("$.updatedBalance.currency").value("826"))
        .andExpect(jsonPath("$.reasonCode").value("00"));
  }

  @Test
  public void shouldReturnResponseFromServiceForAdvice() throws Exception {
    CardAuth cardAuth = mock(CardAuth.class);
    SchemeMessageResponse response = createResponse();

    when(mapper.toDomain(getSchemeMessage())).thenReturn(cardAuth);
    when(processor.advice(cardAuth)).thenReturn(response);

    mockMvc.perform(post("/v1/scheme-message/advice")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authCode").value(1234))
        .andExpect(jsonPath("$.updatedBalance.amount").value("1"))
        .andExpect(jsonPath("$.updatedBalance.currency").value("826"))
        .andExpect(jsonPath("$.reasonCode").value("00"));
  }

  @Test
  public void shouldReturnResponseFromServiceForReversal() throws Exception {
    CardAuth cardAuth = mock(CardAuth.class);
    SchemeMessageResponse response = createResponse();

    when(mapper.toDomainForReversal(getSchemeMessage())).thenReturn(cardAuth);
    when(processor.reversal(cardAuth)).thenReturn(response);

    mockMvc.perform(post("/v1/scheme-message/reversal")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authCode").value("1234"))
        .andExpect(jsonPath("$.updatedBalance.amount").value("1"))
        .andExpect(jsonPath("$.updatedBalance.currency").value("826"))
        .andExpect(jsonPath("$.reasonCode").value("00"));
  }

  @Test
  public void shouldReturnBadRequestOnAuth() throws Exception {
    when(mapper.toDomain(any(SchemeMessage.class)))
        .thenThrow(new RequestValidationFailureException("Request contains null"));

    mockMvc.perform(post("/v1/scheme-message/authorisation")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().json("{\"message\":\"Request contains null\"}", true));
  }

  @Test
  public void shouldReturnBadRequestOnAdvice() throws Exception {
    when(mapper.toDomain(any(SchemeMessage.class)))
        .thenThrow(new RequestValidationFailureException("Request contains null"));

    mockMvc.perform(post("/v1/scheme-message/advice")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().json("{\"message\":\"Request contains null\"}", true));
  }

  @Test
  public void shouldReturnBadRequestOnReversal() throws Exception {
    when(mapper.toDomainForReversal(any(SchemeMessage.class)))
        .thenThrow(new RequestValidationFailureException("Request contains null"));

    mockMvc.perform(post("/v1/scheme-message/reversal")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().json("{\"message\":\"Request contains null\"}", true));
  }


  @Test
  public void shouldReturnUnprocessableEntityOnAuth() throws Exception {
    when(mapper.toDomain(any(SchemeMessage.class)))
        .thenThrow(new CardNotFoundException("Request contains null"));

    mockMvc.perform(post("/v1/scheme-message/authorisation")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().json("{\"message\":\"Request contains null\"}", true));
  }

  @Test
  public void shouldReturnUnprocessableEntityOnAdvice() throws Exception {
    when(mapper.toDomain(any(SchemeMessage.class)))
        .thenThrow(new CardNotFoundException("Request contains null"));

    mockMvc.perform(post("/v1/scheme-message/advice")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().json("{\"message\":\"Request contains null\"}", true));
  }

  @Test
  public void shouldReturnUnprocessableEntityOnReversal() throws Exception {
    when(mapper.toDomainForReversal(any(SchemeMessage.class)))
        .thenThrow(new CardNotFoundException("Request contains null"));

    mockMvc.perform(post("/v1/scheme-message/reversal")
        .content(CARD_AUTH_REQUEST)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().json("{\"message\":\"Request contains null\"}", true));
  }

  private SchemeMessageResponse createResponse() {
    return new SchemeMessageResponse("1234",
        Money.builder().amount(BigDecimal.ONE).currency("826").build(), "00");
  }

  private static final String CARD_AUTH_REQUEST = "{\n"
      + "   \"messageType\":\"Advice\",\n"
      + "   \"processingCode\":\"123456\",\n"
      + "   \"systemTraceNumber\":\"789\",\n"
      + "   \"transaction\":{\n"
      + "      \"amounts\":{\n"
      + "         \"transaction\":{\n"
      + "            \"amount\":1,\n"
      + "            \"currency\":\"826\"\n"
      + "         },\n"
      + "         \"settlement\":{\n"
      + "            \"amount\":2,\n"
      + "            \"currency\":\"826\"\n"
      + "         },\n"
      + "         \"billing\":{\n"
      + "            \"amount\":3,\n"
      + "            \"currency\":\"826\"\n"
      + "         },\n"
      + "         \"fee\":null,\n"
      + "         \"additionalAmounts\":null,\n"
      + "         \"conversionRate\":null\n"
      + "      },\n"
      + "      \"networkCode\":null,\n"
      + "      \"banknetReferenceNumber\":null,\n"
      + "      \"dates\": {\n"
      + "         \"transactionDate\":\"2019-01-01\",\n"
      + "         \"transactionTime\":\"080000\",\n"
      + "         \"settlementDate\":\"2019-01-01\",\n"
      + "         \"conversionDate\":null,\n"
      + "         \"transmissionDateTime\":\"2019-01-01T13:00:00.000Z\"\n"
      + "   },\n"
      + "      \"retrievalReferenceNumber\":null\n"
      + "   },\n"
      + "   \"card\":{\n"
      + "      \"id\":\"777\",\n"
      + "      \"expiryDate\":\"0824\"\n"
      + "   },\n"
      + "   \"acquirer\":{\n"
      + "      \"id\":null,\n"
      + "      \"countryCode\":null\n"
      + "   },\n"
      + "   \"merchant\":{\n"
      + "      \"name\":null,\n"
      + "      \"address\":null,\n"
      + "      \"terminalId\":null,\n"
      + "      \"categoryCode\":\"6006\",\n"
      + "      \"acceptorIdCode\":\"123\",\n"
      + "      \"bankCardPhone\":null\n"
      + "   },\n"
      + "   \"pos\":{\n"
      + "      \"conditionCode\":null,\n"
      + "      \"additionalPosDetail\":\"moreDetails\",\n"
      + "      \"posEntryMode\":null,\n"
      + "      \"extendedDataConditionCodes\":null\n"
      + "   },\n"
      + "   \"additionalData\":null,\n"
      + "   \"fraudScoreData\":null,\n"
      + "   \"reversalAmounts\":null,\n"
      + "   \"authCode\":null,\n"
      + "   \"authResponseCode\":null\n"
      + "}";

  private static final ObjectMapper OBJECT_MAPPER = new
      ObjectMapper().registerModule(new JavaTimeModule())
      .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  private SchemeMessage getSchemeMessage() {
    SchemeMessage message = null;
    try {
      message = OBJECT_MAPPER.readValue(CARD_AUTH_REQUEST, SchemeMessage.class);
    } catch (IOException e) {
      fail();
    }
    return message;
  }

  private static HttpMessageConverter<?> jacksonDateTimeConverter() {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setObjectMapper(OBJECT_MAPPER);
    return converter;
  }

}