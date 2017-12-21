package com.nortal.blaze.auth.http.yupi;

import com.nortal.blaze.auth.User;
import com.nortal.blaze.auth.http.AuthHeaderAuthenticator;
import com.nortal.blaze.auth.http.HttpAuthorization;
import org.apache.cxf.message.Message;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;

@Component(immediate = true, service = AuthHeaderAuthenticator.class)
public class YupiAuthenticator implements AuthHeaderAuthenticator {

  @Override
  public User autheticate(HttpAuthorization auth, Message message) {
    if (!auth.isType(HttpAuthorization.BEARER)) {
      return null;
    }
    String token = auth.getCredential();
    if (token == null) {
      return null;
    }
    if (token.equals("yupi")) {
      User user = new User();
      user.setCode("yupi");
      user.setClaims(Collections.singletonMap("org", "yupland"));
      return user;
    }
    return null;
  }

}
