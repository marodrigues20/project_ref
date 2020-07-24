package com.tenxbanking.cardrails.config.redis;

import com.tenxbanking.cardrails.domain.service.TimeService;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@Component
@ReadingConverter
@AllArgsConstructor
public class BytesToInstantConverter implements Converter<byte[], Instant> {

  private final TimeService timeService;

  @Override
  public Instant convert(final byte[] source) {
    return timeService.toInstant(new String(source));
  }
}