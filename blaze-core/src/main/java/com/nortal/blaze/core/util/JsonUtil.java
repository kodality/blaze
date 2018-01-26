package com.nortal.blaze.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Map;

public final class JsonUtil {
  private static Gson gson = null;

  static {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(Double.class, new WriteDoubleAsInt());
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

  public static Map<String, Object> fromJson(byte[] json) {
    try {
      return fromJson(new String(json, "UTF8"));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private static class WriteDoubleAsInt implements JsonSerializer<Double> {

    @Override
    public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
      return src == src.longValue() ? new JsonPrimitive(src.longValue()) : new JsonPrimitive(src);
    }
  }
}
