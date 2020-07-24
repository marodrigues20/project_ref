package com.tenx.universalbanking.transactionmanager.utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
class JsonPropertyUtil {

  private final Logger logger = getLogger(getClass());

  public JSONObject addPropertyToJson(JSONObject json, String propertyName, String propertyValue) {
    try {
     json.put(propertyName, propertyValue);
    } catch (JSONException e) {
       logger.error("Error occured while adding json property {} with value {} due to {}",propertyName,propertyValue,e);
    }
    return json;
  }

  public String getValueFromJson(JSONObject jsonObject, String propertyName) {

    String value = null;
    try {
      Object propertyValue = jsonObject.has(propertyName) ? jsonObject.get(propertyName) : null;

      value = propertyValue != null ? propertyValue.toString() : null;

    } catch (JSONException e) {
      logger.error("Exception occured while parsing the message json object: {}", e);
    }
    return value;
  }

}
