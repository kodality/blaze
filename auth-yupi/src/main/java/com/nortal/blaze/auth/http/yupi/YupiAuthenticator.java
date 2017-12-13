package com.nortal.blaze.auth.http.yupi;

import com.nortal.blaze.auth.User;
import com.nortal.blaze.auth.http.AuthHeaderAuthenticator;
import com.nortal.blaze.auth.http.HttpAuthorization;
import org.apache.cxf.message.Message;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = AuthHeaderAuthenticator.class)
public class YupiAuthenticator implements AuthHeaderAuthenticator {

  @Override
  public User autheticate(HttpAuthorization auth, Message message) {
    System.out.println("yupi1");
    if (!auth.isType(HttpAuthorization.BEARER)) {
      return null;
    }
    System.out.println("yupi2");
    String token = auth.getCredential();
    if (token == null) {
      return null;
    }
    System.out.println("yupi3");
    if (token.equals("yupi")) {
      User user = new User();
      user.setCode("yupi");
      return user;
    }
    System.out.println("yupi4");
    return null;
  }

}
