package com.nortal.blaze.auth;

import java.util.Map;

public class User {
  private Map<String, Object> claims;

  public Map<String, Object> getClaims() {
    return claims;
  }

  public void setClaims(Map<String, Object> claims) {
    this.claims = claims;
  }

}
