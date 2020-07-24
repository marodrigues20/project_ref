package com.tenxbanking.cardrails.adapter.primary.idempotency;

import static com.tenxbanking.cardrails.adapter.primary.rest.HttpRequestAttribute.IDEMPOTENCY_KEY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenxbanking.cardrails.adapter.primary.rest.model.response.ErrorResponse;
import com.tenxbanking.cardrails.adapter.secondary.redis.IdempotentRepository;
import java.io.IOException;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class IdempotentRequestFilter implements Filter {

  private static final List<String> EXCLUDED_METHODS = List.of(
      HttpMethod.GET.name(),
      HttpMethod.DELETE.name(),
      HttpMethod.TRACE.name(),
      HttpMethod.OPTIONS.name(),
      HttpMethod.HEAD.name()
  );

  private static final String ERROR_MESSAGE = "A request with an identical request body already been processed";
  private static final ErrorResponse ERROR_RESPONSE = new ErrorResponse(ERROR_MESSAGE);

  private final ObjectMapper objectMapper;
  private final MultiReadHttpServletRequestFactory multiReadRequestFactory;
  private final IdempotentRepository idempotentRepository;
  private final RequestHashingService hashingService;

  @Autowired
  public IdempotentRequestFilter(
      ObjectMapper objectMapper,
      MultiReadHttpServletRequestFactory multiReadRequestFactory,
      IdempotentRepository idempotentRepository,
      RequestHashingService hashingService) {
    this.objectMapper = objectMapper;
    this.multiReadRequestFactory = multiReadRequestFactory;
    this.idempotentRepository = idempotentRepository;
    this.hashingService = hashingService;
  }

  @Override
  public void doFilter(
      ServletRequest request,
      ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    HttpServletRequest currentRequest = (HttpServletRequest) request;
    HttpServletResponse currentResponse = (HttpServletResponse) response;

    if (EXCLUDED_METHODS.contains(currentRequest.getMethod())) {
      chain.doFilter(request, response);
      return;
    }

    MultiReadHttpServletRequest multiReadRequest = multiReadRequestFactory.create(currentRequest);

    String body = new String(multiReadRequest.getInputStream().readAllBytes());
    String hash = hashingService.hash(body);

    request.setAttribute(IDEMPOTENCY_KEY, hash);

    if (!idempotentRepository.add(hash)) {
      response.getWriter().write(objectMapper.writeValueAsString(ERROR_RESPONSE));
      currentResponse.setStatus(BAD_REQUEST.value());
      currentResponse.setContentType(APPLICATION_JSON_VALUE);
      return;
    }

    chain.doFilter(multiReadRequest, currentResponse);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void destroy() {

  }

}