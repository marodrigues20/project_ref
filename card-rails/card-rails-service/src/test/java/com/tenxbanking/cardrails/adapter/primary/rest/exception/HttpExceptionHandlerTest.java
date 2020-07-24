package com.tenxbanking.cardrails.adapter.primary.rest.exception;

import static com.tenxbanking.cardrails.adapter.primary.rest.HttpRequestAttribute.IDEMPOTENCY_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.adapter.primary.rest.model.response.ErrorResponse;
import com.tenxbanking.cardrails.adapter.secondary.redis.RedisIdempotentRepository;
import com.tenxbanking.cardrails.domain.exception.CardAuthHandlerUnsupportedException;
import com.tenxbanking.cardrails.domain.exception.CardAuthReservationException;
import com.tenxbanking.cardrails.domain.exception.CardNotFoundException;
import com.tenxbanking.cardrails.domain.exception.CardSettingsNotFoundException;
import com.tenxbanking.cardrails.domain.exception.LimitConstraintException;
import com.tenxbanking.cardrails.domain.exception.SubscriptionNotFoundException;
import com.tenxbanking.cardrails.domain.exception.ValidationException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpExceptionHandlerTest {

  private static final String KEY = "aKey";

  @Mock
  private RedisIdempotentRepository redisIdempotentRepository;
  @InjectMocks
  private HttpExceptionHandler unit;

  @Mock
  private HttpServletRequest request;

  @Test
  void shouldReturnErrorMessageForCardAuthReservationException() {
    CardAuthReservationException exception = new CardAuthReservationException("test");

    ErrorResponse errorResponse = unit.handleCardAuthReservationException(exception, request);

    assertThat(errorResponse.getMessage()).isEqualTo("test");
  }

  @Test
  void shouldReturnErrorMessageForRequestValidationFailureException() {
    RequestValidationFailureException exception = new RequestValidationFailureException("test");

    ErrorResponse errorResponse = unit.handleRequestValidationFailureException(exception, request);

    assertThat(errorResponse.getMessage()).isEqualTo("test");
  }

  @Test
  void shouldReturnErrorMessageForCardAuthHandlerUnsupportedException() {
    CardAuthHandlerUnsupportedException exception = new CardAuthHandlerUnsupportedException("test");

    ErrorResponse errorResponse = unit.handleCardAuthHandlerUnsupportedException(exception, request);

    assertThat(errorResponse.getMessage()).isEqualTo("test");
  }

  @Test
  void shouldReturnErrorMessageForUnsolicitedReversalException() {
    UnsolicitedReversalException exception = new UnsolicitedReversalException("test");

    ErrorResponse errorResponse = unit.handleUnsolicitedReversalExceptionException(exception, request);

    assertThat(errorResponse.getMessage()).isEqualTo("test");
  }

  @Test
  void shouldReturnErrorMessageForCardSettingsNotFoundException() {
    CardSettingsNotFoundException exception = new CardSettingsNotFoundException();

    ErrorResponse errorResponse = unit.cardSettingsNotFoundException(exception, request);

    assertThat(errorResponse.getMessage()).isEqualTo("Card settings cannot be found");
  }

  @Test
  void shouldNotRemoveIdempotencyKeyIfNotPresentInRequest() {
    CardAuthReservationException exception = new CardAuthReservationException("test");

    unit.handleCardAuthReservationException(exception, request);

    verifyZeroInteractions(redisIdempotentRepository);
  }

  @Test
  void shouldRemoveIdempotencyKeyForCardAuthReservationException() {
    CardAuthReservationException exception = new CardAuthReservationException("test");
    when(request.getAttribute(IDEMPOTENCY_KEY)).thenReturn(KEY);

    unit.handleCardAuthReservationException(exception, request);

    verify(redisIdempotentRepository).remove(KEY);
  }

  @Test
  void shouldRemoveIdempotencyKeyForRequestValidationFailureException() {
    RequestValidationFailureException exception = new RequestValidationFailureException("test");
    when(request.getAttribute(IDEMPOTENCY_KEY)).thenReturn(KEY);

    unit.handleRequestValidationFailureException(exception, request);

    verify(redisIdempotentRepository).remove(KEY);
  }

  @Test
  void shouldRemoveIdempotencyKeyForLimitConstraintException() {
    LimitConstraintException exception = new LimitConstraintException("test");
    when(request.getAttribute(IDEMPOTENCY_KEY)).thenReturn(KEY);

    unit.handleLimitConstraintException(exception, request);

    verify(redisIdempotentRepository).remove(KEY);
  }

  @Test
  void shouldRemoveIdempotencyKeyForCardAuthHandlerUnsupportedException() {
    CardAuthHandlerUnsupportedException exception = new CardAuthHandlerUnsupportedException("test");
    when(request.getAttribute(IDEMPOTENCY_KEY)).thenReturn(KEY);

    unit.handleCardAuthHandlerUnsupportedException(exception, request);

    verify(redisIdempotentRepository).remove(KEY);
  }

  @Test
  void shouldRemoveIdempotencyKeyForCardNotFoundException() {
    CardNotFoundException exception = new CardNotFoundException();
    when(request.getAttribute(IDEMPOTENCY_KEY)).thenReturn(KEY);

    unit.cardNotFoundException(exception, request);

    verify(redisIdempotentRepository).remove(KEY);
  }

  @Test
  void shouldRemoveIdempotencyKeyForCardSettingsNotFoundException() {
    CardSettingsNotFoundException exception = new CardSettingsNotFoundException();
    when(request.getAttribute(IDEMPOTENCY_KEY)).thenReturn(KEY);

    unit.cardSettingsNotFoundException(exception, request);

    verify(redisIdempotentRepository).remove(KEY);
  }

  @Test
  void shouldRemoveIdempotencyKeyForSubscriptionNotFoundException() {
    SubscriptionNotFoundException exception = new SubscriptionNotFoundException();
    when(request.getAttribute(IDEMPOTENCY_KEY)).thenReturn(KEY);

    unit.subscriptionNotFoundException(exception, request);

    verify(redisIdempotentRepository).remove(KEY);
  }

  @Test
  void shouldRemoveIdempotencyKeyForValidationException() {
    ValidationException exception = new ValidationException(List.of());
    when(request.getAttribute(IDEMPOTENCY_KEY)).thenReturn(KEY);

    unit.validationException(exception, request);

    verify(redisIdempotentRepository).remove(KEY);
  }

  @Test
  void shouldRemoveIdempotencyKeyForUnsolicitedReversalException() {
    UnsolicitedReversalException exception = new UnsolicitedReversalException("asd");
    when(request.getAttribute(IDEMPOTENCY_KEY)).thenReturn(KEY);

    unit.handleUnsolicitedReversalExceptionException(exception, request);

    verify(redisIdempotentRepository).remove(KEY);
  }

  @Test
  void shouldRemoveIdempotencyKeyForException() {
    RuntimeException exception = new RuntimeException("asd");
    when(request.getAttribute(IDEMPOTENCY_KEY)).thenReturn(KEY);

    unit.handleException(exception, request);

    verify(redisIdempotentRepository).remove(KEY);
  }

}