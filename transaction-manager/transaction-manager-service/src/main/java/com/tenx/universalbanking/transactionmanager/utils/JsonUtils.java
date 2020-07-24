package com.tenx.universalbanking.transactionmanager.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import org.springframework.stereotype.Component;

@Component
public class JsonUtils {

  public static String jsonToString(Object jsonObject) {
    String json;

    if (jsonObject == null) {
      json = "Null Object";
    } else {
      ObjectMapper mapper = new ObjectMapper();
      registerModule(mapper);
      try {
        json = mapper.writeValueAsString(jsonObject);
      } catch (Exception e) {
        json = "Object could not be converted to Json Format";
      }
    }

    return json;
  }

  public static <T> T stringToJson(String jsonString, Class<T> clazz) {
    T jsonObject;

    if (jsonString == null) {
      jsonObject = null;
    } else {
      ObjectMapper mapper = new ObjectMapper();
      registerModule(mapper);
      try {
        jsonObject = mapper.readValue(jsonString, clazz);
      } catch (Exception e) {
        jsonObject = null;
      }
    }

    return jsonObject;
  }

  public static <T> String toJson(T object) {
    ObjectMapper mapper = new ObjectMapper();
    registerModule(mapper);
    try {
        return mapper.writeValueAsString(object);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
  
  private static void registerModule(ObjectMapper mapper) {
    mapper.registerModule(new JsonOrgModule());
  }
}
