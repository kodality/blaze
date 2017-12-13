package com.nortal.blaze.auth.http;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public class HttpAuthorization {
  public static final String BASIC = "Basic";
  public static final String BEARER = "Bearer";

  private static final String UTF8 = "UTF8";
  private static final String[] types = { BASIC, BEARER };

  private final String type;
  private final String credential;

  private HttpAuthorization() {
    this(null, null);
  }

  private HttpAuthorization(String type, String credential) {
    this.type = type;
    this.credential = credential;
  }

  public String getType() {
    return type;
  }

  public String getCredential() {
    return credential;
  }

  public boolean isType(String type) {
    return StringUtils.equals(type, this.type);
  }

  public static HttpAuthorization parse(String header) {
    if (StringUtils.isEmpty(header)) {
      return new HttpAuthorization();
    }
    String[] parts = header.split("\\s");
    if (parts.length != 2) {
      // throw new ApiException(401, "invalid authorization header");
      return new HttpAuthorization();
    }
    if (!Stream.of(types).anyMatch(t -> parts[0].equalsIgnoreCase(t))) {
      // throw new ApiException(401, "unsupported auth type");
      return new HttpAuthorization();
    }
    return new HttpAuthorization(parts[0], parts[1]);
  }

  public String getCredentialDecoded() {
    try {
      return new String(Base64.getDecoder().decode(credential), UTF8);
    } catch (IllegalArgumentException | UnsupportedEncodingException e) {
      return credential;
    }
  }
}
