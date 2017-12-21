package com.nortal.blaze.auth;

import java.util.Map;

public class User {
  private String code;
  private Map<String, String> claims;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Map<String, String> getClaims() {
    return claims;
  }

  public void setClaims(Map<String, String> claims) {
    this.claims = claims;
  }

}
