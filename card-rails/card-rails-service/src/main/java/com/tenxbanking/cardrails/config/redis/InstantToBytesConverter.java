package com.tenxbanking.cardrails.config.redis;

import com.tenxbanking.cardrails.domain.service.TimeService;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
@AllArgsConstructor
public class InstantToBytesConverter implements Converter<Instant, byte[]> {

  private final TimeService timeService;

  @Override
  public byte[] convert(final Instant source) {
    return timeService.fromInstant(source).getBytes();
  }
}