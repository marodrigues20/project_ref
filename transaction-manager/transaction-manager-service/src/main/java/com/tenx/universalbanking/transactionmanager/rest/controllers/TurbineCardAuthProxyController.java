package com.tenx.universalbanking.transactionmanager.rest.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tenx.universalbanking.transactionmanager.rest.mapper.CardAuthRequestMapper;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.SchemeMessage;
import com.tenx.universalbanking.transactionmanager.rest.responses.turbine.SchemeMessageResponse;
import com.tenx.universalbanking.transactionmanager.service.turbine.TurbineAuthService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
class TurbineCardAuthProxyController {

  private final TurbineAuthService service;
  private final CardAuthRequestMapper mapper;

  @ApiOperation(value = "Transaction Authorisation", nickname = "pOSTV1TransactionAuthorisation", response = SchemeMessageResponse.class, tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Valid response", response = SchemeMessageResponse.class),
      @ApiResponse(code = 400, message = "Bad data in the request", response = Error.class),
      @ApiResponse(code = 500, message = "Some internal error", response = Error.class)})
  @PostMapping(value = "/v1/scheme-message/authorisation",
      produces = APPLICATION_JSON_VALUE,
      consumes = APPLICATION_JSON_VALUE)
  ResponseEntity<SchemeMessageResponse> auth(@Valid @RequestBody SchemeMessage body) {
    SchemeMessageResponse response = service.authorise(mapper.toDomain(body));
    return ResponseEntity.ok(response);
  }

  @ApiOperation(value = "Transaction advice", nickname = "pOSTV1TransactionAdvice", response = SchemeMessageResponse.class, tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Valid response", response = SchemeMessageResponse.class),
      @ApiResponse(code = 400, message = "Bad data in the request such as unknown cardId", response = Error.class),
      @ApiResponse(code = 500, message = "Some internal error", response = Error.class)})
  @PostMapping(value = "/v1/scheme-message/advice",
      produces = APPLICATION_JSON_VALUE,
      consumes = APPLICATION_JSON_VALUE)
  ResponseEntity<SchemeMessageResponse> advice(@Valid @RequestBody SchemeMessage body) {
    SchemeMessageResponse response = service.advice(mapper.toDomain(body));
    return ResponseEntity.ok(response);
  }

  @ApiOperation(value = "Transaction reversal", nickname = "pOSTV1TransactionReversal", response = SchemeMessageResponse.class, tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Valid response", response = SchemeMessageResponse.class),
      @ApiResponse(code = 400, message = "Bad data in the request such as unknown cardId", response = Error.class),
      @ApiResponse(code = 500, message = "Some internal error", response = Error.class)})
  @PostMapping(value = "/v1/scheme-message/reversal",
      produces = APPLICATION_JSON_VALUE,
      consumes = APPLICATION_JSON_VALUE)
  ResponseEntity<SchemeMessageResponse> reversal(@Valid @RequestBody SchemeMessage body) {
    SchemeMessageResponse response = service.reversal(mapper.toDomainForReversal(body));
    return ResponseEntity.ok(response);
  }

}
