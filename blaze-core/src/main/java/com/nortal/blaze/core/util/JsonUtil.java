package com.nortal.blaze.core.util;

import com.google.gson.Gson;

import java.util.Map;

public final class JsonUtil {
  private JsonUtil() {
    //
  }

  public static String toJson(Object o) {
    return new Gson().toJson(o);
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> fromJson(String json) {
    return new Gson().fromJson(json, Map.class);
  }
}
