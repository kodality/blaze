package com.nortal.blaze.auth.http;

import com.nortal.blaze.auth.ClientIdentity;
import com.nortal.blaze.auth.User;
import com.nortal.blaze.core.exception.FhirException;
import com.nortal.fhir.rest.filter.InInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

@Component(immediate = true, service = InInterceptor.class)
@Slf4j
public class HttpAuthInterceptor extends InInterceptor {
  private static final String AUTHORIZATION = "Authorization";
  @Reference
  private ClientIdentity clientIdentity;
  @Reference
  private final List<AuthHeaderAuthenticator> authenticators = new ArrayList<>();

  public HttpAuthInterceptor() {
    super(Phase.READ);
  }

  @Override
  public void handle(Message message) {
    HttpServletRequest request = (HttpServletRequest) message.get("HTTP.REQUEST");

    if (clientIdentity.isAuthenticated()) {
      throw new IllegalStateException("context cleanup not worked, panic");
    }
    String authorizationHeader = request.getHeader(AUTHORIZATION);
    HttpAuthorization auth = HttpAuthorization.parse(authorizationHeader);

    User user =
        authenticators.stream().map(a -> a.autheticate(auth, message)).filter(a -> a != null).findFirst().orElse(null);
    clientIdentity.set(user);

    if (!clientIdentity.isAuthenticated()) {
      log.debug("could not authenticate. tried services: " + authenticators);
      throw new FhirException(401, "not authenticated");
    }

  }

  protected void bind(AuthHeaderAuthenticator authenticator) {
    authenticators.add(authenticator);
  }

  protected void unbind(AuthHeaderAuthenticator authenticator) {
    authenticators.remove(authenticator);
  }

}
