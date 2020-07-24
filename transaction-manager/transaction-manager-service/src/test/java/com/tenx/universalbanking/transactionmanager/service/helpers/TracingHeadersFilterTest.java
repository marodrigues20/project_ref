package com.tenx.universalbanking.transactionmanager.service.helpers;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class TracingHeadersFilterTest {

  @InjectMocks
  private TracingHeadersFilter tracingHeadersFilter;

  @Mock
  private HttpServletRequest request;

  @Before
  public void setup() {
    tracingHeadersFilter.init();
    ReflectionTestUtils.setField(
        tracingHeadersFilter,
        "headersForward",
        "traceid,X-,l5d-");
  }

  @Test
  public void filterTest() {
    when(request.getHeaderNames()).thenReturn(getHeaderNames());
    when(request.getHeader(eq("traceid"))).thenReturn("trace123");
    when(request.getHeader(eq("x-b3-parentspanid"))).thenReturn("xb3123");
    when(request.getHeader(eq("l5d-x-test"))).thenReturn("l5d123");

    Map<String, String> filtered = tracingHeadersFilter.filter(request);
    Assert.assertNull(filtered.get("content-type"));
    Assert.assertEquals("trace123", filtered.get("traceid"));
    Assert.assertEquals("xb3123", filtered.get("x-b3-parentspanid"));
    Assert.assertEquals("l5d123", filtered.get("l5d-x-test"));
  }

  private Enumeration<String> getHeaderNames() {
    return Collections.enumeration(
        Arrays.asList("traceid", "x-b3-parentspanid", "l5d-x-test", "content-type", "REQUEST_ID")
    );
  }
}
