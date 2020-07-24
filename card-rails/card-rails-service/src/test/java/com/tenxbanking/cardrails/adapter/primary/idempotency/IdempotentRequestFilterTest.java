package com.tenxbanking.cardrails.adapter.primary.idempotency;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenxbanking.cardrails.adapter.secondary.redis.IdempotentRepository;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IdempotentRequestFilterTest {

  @Mock
  private MultiReadHttpServletRequestFactory multiReadRequestFactory;
  @Mock
  private IdempotentRepository idempotentRepository;
  @Mock
  private RequestHashingService hashingService;
  private IdempotentRequestFilter underTest;

  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private FilterChain filterChain;

  @BeforeEach
  void setup() {
    underTest = new IdempotentRequestFilter(
        new ObjectMapper(),
        multiReadRequestFactory,
        idempotentRepository,
        hashingService
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {"GET", "DELETE", "TRACE", "OPTIONS", "HEAD"})
  void excludesHttpMethodsInExclusionList(String method) throws IOException, ServletException {

    when(request.getMethod()).thenReturn(method);

    underTest.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyZeroInteractions(idempotentRepository, hashingService, request);
  }

  @Test
  void respondsWithBadRequestIfIdempotencyCheckFails() throws IOException, ServletException {

    String body = "aStringBody";
    String hash = "aStringHash";
    ServletInputStream stream = mock(ServletInputStream.class);
    PrintWriter writer = mock(PrintWriter.class);
    MultiReadHttpServletRequest multiReadRequest = mock(MultiReadHttpServletRequest.class);

    when(multiReadRequest.getInputStream()).thenReturn(stream);
    when(multiReadRequestFactory.create(request)).thenReturn(multiReadRequest);
    when(stream.readAllBytes()).thenReturn(body.getBytes());
    when(response.getWriter()).thenReturn(writer);
    when(hashingService.hash(body)).thenReturn(hash);
    when(idempotentRepository.add(hash)).thenReturn(false);
    when(request.getMethod()).thenReturn("POST");

    underTest.doFilter(request, response, filterChain);

    verify(hashingService).hash(body);
    verify(idempotentRepository).add(hash);
    verify(writer).write("{\"message\":\"A request with an identical request body already been processed\",\"code\":null}");
    verify(response).setStatus(400);
    verify(response).setContentType("application/json");
    verifyZeroInteractions(filterChain);

  }

}