package com.tenx.tsys.proxybatch.util;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.tenx.tsys.proxybatch.utils.JsonUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JsonUtilsTest {

  @Test
  public void jsonToStringSuccess() {
    assertThat(JsonUtils.jsonToString(12)).isEqualTo("12");
  }

  @Test
  public void jsonToStringNull() {
    assertThat(JsonUtils.jsonToString(null)).isEqualTo("Null Object");
  }

  @Test
  public void stringToJsonSuccess() {
    assertThat(JsonUtils.stringToJson("12", Integer.class)).isEqualTo(12);
  }

  @Test
  public void stringToJsonNull() {
    assertThat(JsonUtils.stringToJson(null, Integer.class)).isEqualTo(null);
  }

  @Test
  public void stringToJsonException() {
    assertThat(JsonUtils.stringToJson("---", Integer.class)).isEqualTo(null);
  }

}
