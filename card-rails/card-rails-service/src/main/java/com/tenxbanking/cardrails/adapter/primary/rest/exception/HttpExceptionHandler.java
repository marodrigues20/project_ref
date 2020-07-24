package com.tenxbanking.cardrails.adapter.primary.rest.exception;

import static com.tenxbanking.cardrails.adapter.primary.rest.HttpRequestAttribute.IDEMPOTENCY_KEY;

import com.tenxbanking.cardrails.adapter.primary.rest.model.response.ErrorResponse;
import com.tenxbanking.cardrails.adapter.primary.rest.model.response.SchemeMessageResponse;
import com.tenxbanking.cardrails.adapter.secondary.redis.RedisIdempotentRepository;
import com.tenxbanking.cardrails.domain.exception.CardAuthHandlerUnsupportedException;
import com.tenxbanking.cardrails.domain.exception.CardAuthReservationException;
import com.tenxbanking.cardrails.domain.exception.CardNotFoundException;
import com.tenxbanking.cardrails.domain.exception.CardSettingsNotFoundException;
import com.tenxbanking.cardrails.domain.exception.LimitConstraintException;
import com.tenxbanking.cardrails.domain.exception.SubscriptionNotFoundException;
import com.tenxbanking.cardrails.domain.exception.ValidationException;
import com.tenxbanking.cardrails.domain.model.Cain002;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@AllArgsConstructor
public class HttpExceptionHandler {

  private static final String UNSOLICITED_REVERSAL_CODE = "76";

  private final RedisIdempotentRepository redisIdempotentRepository;

  @ExceptionHandler(CardAuthReservationException.class)
  @ResponseStatus(HttpStatus.BAD_GATEWAY)
  public ErrorResponse handleCardAuthReservationException(
      CardAuthReservationException e,
      HttpServletRequest request) {
    log.info("handling exception ", e);
    removeIdempotencyKey(request);
    return new ErrorResponse(e.getMessage());
  }

  @ExceptionHandler(RequestValidationFailureException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleRequestValidationFailureException(
      RequestValidationFailureException e,
      HttpServletRequest request) {
    log.info("handling exception ", e);
    removeIdempotencyKey(request);
    return new ErrorResponse(e.getMessage());
  }

  @ExceptionHandler(value = {LimitConstraintException.class})
  public ResponseEntity<SchemeMessageResponse> handleLimitConstraintException(LimitConstraintException e,
      HttpServletRequest request) {
    log.info("handling exception ", e);
    removeIdempotencyKey(request);
    return ResponseEntity.ok(SchemeMessageResponse.of(Cain002.unsuccessful()));
  }

  @ExceptionHandler(CardAuthHandlerUnsupportedException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public ErrorResponse handleCardAuthHandlerUnsupportedException(
      CardAuthHandlerUnsupportedException e,
      HttpServletRequest request) {
    log.info("handling exception ", e);
    removeIdempotencyKey(request);
    return new ErrorResponse(e.getMessage());
  }

  @ExceptionHandler(CardNotFoundException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public ErrorResponse cardNotFoundException(
      CardNotFoundException e,
      HttpServletRequest request) {
    log.info("handling exception ", e);
    removeIdempotencyKey(request);
    return new ErrorResponse(e.getMessage());
  }

  @ExceptionHandler(CardSettingsNotFoundException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public ErrorResponse cardSettingsNotFoundException(
      CardSettingsNotFoundException e,
      HttpServletRequest request) {
    log.info("handling exception ", e);
    removeIdempotencyKey(request);
    return new ErrorResponse(e.getMessage());
  }


  @ExceptionHandler(SubscriptionNotFoundException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public ErrorResponse subscriptionNotFoundException(
      SubscriptionNotFoundException e,
      HttpServletRequest request) {
    log.info("handling exception ", e);
    removeIdempotencyKey(request);
    return new ErrorResponse(e.getMessage());
  }

  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public ErrorResponse validationException(
      ValidationException e,
      HttpServletRequest request) {
    log.info("handling exception ", e);
    removeIdempotencyKey(request);
    return new ErrorResponse(e.getMessage());
  }

  @ExceptionHandler(UnsolicitedReversalException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleUnsolicitedReversalExceptionException(
      UnsolicitedReversalException e,
      HttpServletRequest request) {
    log.info("handling exception ", e);
    removeIdempotencyKey(request);
    return new ErrorResponse(e.getMessage(), UNSOLICITED_REVERSAL_CODE);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleException(Exception e, HttpServletRequest request) {
    log.error("handling exception ", e);
    removeIdempotencyKey(request);
    return new ErrorResponse(e.getMessage());
  }

  private void removeIdempotencyKey(HttpServletRequest request) {
    Optional.ofNullable((String) request.getAttribute(IDEMPOTENCY_KEY))
        .ifPresent(redisIdempotentRepository::remove);
  }

}
