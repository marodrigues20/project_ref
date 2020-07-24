package com.tenx.universalbanking.transactionmanager.service.helpers;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class TracingHeadersFilter {

  @Value("${kafka.headers-forward}")
  private String headersForward;

  private Supplier<Stream<String>> requiredHeadersStreamSupplier;

  @PostConstruct
  public void init() {
    requiredHeadersStreamSupplier = () -> Arrays.stream(headersForward.split(","));
  }

  /**
   * This function filters HttpServletRequest by required headers specified in
   * kafka.headers-forward and returns as Map
   *
   * @param request
   * @return Map
   */
  public Map<String, String> filter(HttpServletRequest request) {

    return Collections.list(request.getHeaderNames()).stream()
        .filter(requestHeaderName ->
            requiredHeadersStreamSupplier.get()
                .anyMatch(requiredHeader -> requestHeaderName.toLowerCase(Locale.ENGLISH).startsWith(requiredHeader.toLowerCase(Locale.ENGLISH))))
        .collect(
            Collectors.toMap(
                Function.identity(),
                request::getHeader));
  }
}

