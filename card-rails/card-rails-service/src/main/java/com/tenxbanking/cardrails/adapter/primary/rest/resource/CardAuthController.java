package com.tenxbanking.cardrails.adapter.primary.rest.resource;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.ADVICE;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.REVERSAL;

import com.tenxbanking.cardrails.adapter.primary.rest.mapper.Cain001Mapper;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.SchemeMessage;
import com.tenxbanking.cardrails.adapter.primary.rest.model.response.SchemeMessageResponse;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.service.handler.CardAdviceHandler;
import com.tenxbanking.cardrails.domain.service.handler.CardAuthHandler;
import com.tenxbanking.cardrails.domain.service.handler.CardAuthReversalHandler;
import com.tenxbanking.cardrails.domain.validator.RequestValidator;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@AllArgsConstructor
public class CardAuthController {

  private final CardAuthHandler cardAuthHandler;
  private final CardAdviceHandler cardAdviceHandler;
  private final CardAuthReversalHandler cardAuthReversalHandler;
  private final Cain001Mapper cain001Mapper;
  private final RequestValidator requestValidator;

  @ApiOperation(value = "Card authorisation", nickname = "authorizeCardTransaction", response = SchemeMessageResponse.class)
  @PostMapping("/authorisation")
  public ResponseEntity<SchemeMessageResponse> authorizeCardTransaction(
      @ApiParam(value = "card authorisation request payload", required = true)
      @Validated @RequestBody final SchemeMessage schemeMessage) {

    Cain001 request = cain001Mapper.toDomain(schemeMessage, AUTH);
    Cain002 response = cardAuthHandler.auth(request);

    return ResponseEntity.ok(SchemeMessageResponse.of(response));
  }

  @ApiOperation(value = "Card advice", nickname = "aadviceCardTransaction", response = SchemeMessageResponse.class)
  @PostMapping("/authorisation_advice")
  public ResponseEntity<SchemeMessageResponse> adviceCardTransaction(
      @ApiParam(value = "card authorisation request payload", required = true)
      @Validated @RequestBody final SchemeMessage schemeMessage) {

    Cain001 request = cain001Mapper.toDomain(schemeMessage, ADVICE);
    Cain002 response = cardAdviceHandler.auth(request);

    return ResponseEntity.ok(SchemeMessageResponse.of(response));
  }

  @ApiOperation(value = "Card authorisation reversal", nickname = "reversalCardTransaction", response = SchemeMessageResponse.class)
  @PostMapping("/reversal_advice")
  public ResponseEntity<SchemeMessageResponse> reversalCardTransaction(
      @ApiParam(value = "card reversal request payload", required = true)
      @Validated @RequestBody final SchemeMessage schemeMessage) {

    requestValidator.validate(schemeMessage);

    Cain001 request = cain001Mapper.toDomain(schemeMessage, REVERSAL);
    Cain002 response = cardAuthReversalHandler.auth(request);

    return ResponseEntity.ok(SchemeMessageResponse.of(response));
  }

}
