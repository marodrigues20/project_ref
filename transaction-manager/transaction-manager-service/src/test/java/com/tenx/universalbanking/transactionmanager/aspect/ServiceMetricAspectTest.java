package com.tenx.universalbanking.transactionmanager.aspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
class ServiceMetricAspectTest {

  @Mock
  private MeterRegistry meterRegistry;

  @Mock
  private  Counter counter;

  @Mock
  private ProceedingJoinPoint proceedingJoinPoint;

  @InjectMocks
  private ServiceMetricAspect serviceMetricAspect;

  @Test
  public void incrementTransactionPaymentTest() throws Throwable {
    Object object = new Object();
    ReflectionTestUtils.setField(serviceMetricAspect,"transactionCounter", counter);
    doNothing().when(counter).increment();
    when(proceedingJoinPoint.proceed()).thenReturn(object);
    Object actual = serviceMetricAspect.incrementTransactionPayment(proceedingJoinPoint);
    assertEquals(object, actual);
    verify(counter).increment();
  }

}
