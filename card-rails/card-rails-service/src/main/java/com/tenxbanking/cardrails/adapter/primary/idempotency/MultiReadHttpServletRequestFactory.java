package com.tenxbanking.cardrails.adapter.primary.idempotency;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class MultiReadHttpServletRequestFactory {

  public MultiReadHttpServletRequest create(HttpServletRequest request) {
    return new MultiReadHttpServletRequest(request);
  }

}
