package com.nortal.blaze.auth.http;

import com.nortal.blaze.auth.User;
import org.apache.cxf.message.Message;

public interface AuthHeaderAuthenticator {
  User autheticate(HttpAuthorization auth, Message message);
}
