package com.nortal.blaze.auth;

import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = ClientIdentity.class)
public class ClientIdentity {
  private final ThreadLocal<User> context = new InheritableThreadLocal<>();

  public void set(User user) {
    context.set(user);
  }

  public User get() {
    return context.get();
  }

  public boolean isAuthenticated() {
    return context.get() != null;
  }

  public void remove() {
    context.remove();
  }

}
