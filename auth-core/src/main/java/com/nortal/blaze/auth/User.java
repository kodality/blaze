package com.nortal.blaze.auth;

import java.util.Map;

public class User {
  private String code;
  private Map<String, Object> claims;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Map<String, Object> getClaims() {
    return claims;
  }

  public void setClaims(Map<String, Object> claims) {
    this.claims = claims;
  }

}
