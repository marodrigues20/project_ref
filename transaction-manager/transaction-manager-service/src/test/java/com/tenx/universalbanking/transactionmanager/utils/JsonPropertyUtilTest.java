package com.tenx.universalbanking.transactionmanager.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class JsonPropertyUtilTest {

  private final JsonPropertyUtil creator = new JsonPropertyUtil();

  private static final String PROPERTY_NAME = "testPropertyName";
  private static final String PROPERTY_VALUE = "testPropertyValue";


  @Test
  public void addPropertyToJson_shouldAppendValue() throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("test", "test");

    JSONObject actualJson = creator.addPropertyToJson(jsonObject, PROPERTY_NAME, PROPERTY_VALUE);

    assertThat(actualJson.get(PROPERTY_NAME)).isEqualTo(PROPERTY_VALUE);
    assertThat(actualJson.get("test")).isEqualTo("test");
  }

  @Test
  public void addPropertyToJson_shouldFailSilently_whenAddPropertyFails() throws JSONException {
    JSONObject jsonObject = mock(JSONObject.class);
    when(jsonObject.put(PROPERTY_NAME, PROPERTY_VALUE))
        .thenThrow(new JSONException("testing exception"));

    JSONObject actualJson = creator.addPropertyToJson(jsonObject, PROPERTY_NAME, PROPERTY_VALUE);

    assertThat(actualJson).isNotNull();
  }

  @Test
  public void getValueFromJson_shouldReturnExpectedValue_whenJsonContainsIt() throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("test", "test");

    String actual = creator.getValueFromJson(jsonObject, "test");

    assertThat(actual).isEqualTo("test");
  }

  @Test
  public void getValueFromJson_shouldReturnNull_whenJsonMissingIt() {
    JSONObject jsonObject = new JSONObject();

    String actual = creator.getValueFromJson(jsonObject, "MISSING_VALUE");

    assertThat(actual).isNull();
  }

  @Test
  public void getValueFromJson_shouldReturnNull_whenRetrievingItFromObjectFails()
      throws JSONException {
    JSONObject jsonObject = mock(JSONObject.class);
    when(jsonObject.has(PROPERTY_NAME)).thenReturn(true);
    when(jsonObject.get(PROPERTY_NAME)).thenThrow(new JSONException("testing exception"));

    String actual = creator.getValueFromJson(jsonObject, PROPERTY_NAME);

    assertThat(actual).isNull();
  }
}