package com.nortal.blaze.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

public final class JsonUtil {
  private static Gson gson = null;

  static {
    GsonBuilder builder = new GsonBuilder();
    gson = builder.create();
  }

  private JsonUtil() {
    //
  }

  public static String toJson(Object o) {
    return gson.toJson(o);
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> fromJson(String json) {
    return gson.fromJson(json, Map.class);
  }
}
