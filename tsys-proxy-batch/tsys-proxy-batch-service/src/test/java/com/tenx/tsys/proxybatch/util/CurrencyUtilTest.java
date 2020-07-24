package com.tenx.tsys.proxybatch.util;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.tenx.tsys.proxybatch.utils.CurrencyUtil;
import java.util.Currency;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CurrencyUtilTest {

  @Test
  public void getCurrencyCodeGBP() {
    String currencyCode = CurrencyUtil.getCurrencyCode(826);
    assertThat(currencyCode).isEqualTo("GBP");
  }

  @Test
  public void getCurrencyCodeAUD() {
    String currencyCode = CurrencyUtil.getCurrencyCode(Integer.valueOf("036"));
    assertThat(currencyCode).isEqualTo("AUD");
  }

  @Test
  public void getNullCurrencyCode() {
    String currencyCode = CurrencyUtil.getCurrencyCode(82600);
    assertThat(currencyCode).isNull();
  }

  @Test
  public void getCurrencyInstance() {
    Currency currency = CurrencyUtil.getCurrencyInstance(826);
    assertThat(currency.getCurrencyCode()).isEqualTo("GBP");
  }

  @Test
  public void getNullCurrencyInstance() {
    Currency currency = CurrencyUtil.getCurrencyInstance(82600);
    assertThat(currency).isNull();
  }

  @Test
  public void getCurrencyNumericCode() {
    assertThat(CurrencyUtil.getCurrencyNumericCode("GBP")).isEqualTo(826);
  }
}
