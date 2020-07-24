package com.tenx.universalbanking.transactionmanager.aspect;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
class ServiceMetricAspect {

  private final Counter transactionCounter;

  public ServiceMetricAspect(MeterRegistry registry) {
    transactionCounter = Counter
        .builder("payment_transactions_processed")
        .description("Number of payment transactions processed")
        .register(registry);
  }

  @Around("@annotation(com.tenx.universalbanking.transactionmanager.aspect.annotations.PaymentCountMetric)")
  public Object incrementTransactionPayment(ProceedingJoinPoint joinPoint) throws Throwable {
    transactionCounter.increment();
    return joinPoint.proceed();
  }

}
