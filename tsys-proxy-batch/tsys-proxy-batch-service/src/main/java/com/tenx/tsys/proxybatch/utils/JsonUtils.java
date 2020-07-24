package com.tenx.tsys.proxybatch.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;

public class JsonUtils {

  private JsonUtils() {
  }

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
        json = "Object could not be converted to String";
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

  private static void registerModule(ObjectMapper mapper) {
    mapper.registerModule(new JsonOrgModule());
  }
}
