package com.nortal.blaze.auth;

import com.nortal.blaze.core.exception.FhirException;
import com.nortal.fhir.rest.filter.InInterceptor;
import javax.servlet.http.HttpServletRequest;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = InInterceptor.class)
public class TokenCheckInterceptor extends InInterceptor {
  private static final String AUTHORIZATION = "Authorization";
  @Reference
  private ClientIdentity clientIdentity;

  public TokenCheckInterceptor() {
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

    if (auth.isType(HttpAuthorization.BEARER)) {
      clientIdentity.set(bearerAuth(auth));
    }

    if (!clientIdentity.isAuthenticated()) {
      throw new FhirException(401, "not authenticated");
    }

  }

  private User bearerAuth(HttpAuthorization auth) {
    String token = auth.getCredential();
    if (token == null) {
      return null;
    }
    if (token.equals("yupi")) {
      User user = new User();
      user.setCode("yupi");
      return user;
    }
    return null;
  }

}
