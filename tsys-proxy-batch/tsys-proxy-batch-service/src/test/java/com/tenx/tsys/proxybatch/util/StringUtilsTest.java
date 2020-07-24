package com.tenx.tsys.proxybatch.util;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.tenx.tsys.proxybatch.utils.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StringUtilsTest {

  @Test
  public void getMaskedMessage() {
    String maskedMessage = "1234567890123450";
    assertThat(StringUtils.maskMessage(maskedMessage)).isEqualTo("XXXXXXXXXXXX3450");
  }

  @Test
  public void getMaskedMessageAsNull() {
    String maskedMessage = null;
    assertThat(StringUtils.maskMessage(maskedMessage)).isEqualTo(null);
  }
}
