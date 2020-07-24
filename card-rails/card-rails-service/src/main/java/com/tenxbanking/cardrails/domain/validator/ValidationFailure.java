package com.tenxbanking.cardrails.domain.validator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.codehaus.jackson.annotate.JsonAnyGetter;

@Getter
@ToString
@Builder
@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
public class ValidationFailure {

  private final String message;

}
