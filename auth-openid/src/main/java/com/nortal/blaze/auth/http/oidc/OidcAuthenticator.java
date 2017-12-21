package com.nortal.blaze.auth.http.oidc;

import com.google.gson.Gson;
import com.nortal.blaze.auth.ClientIdentity;
import com.nortal.blaze.auth.User;
import com.nortal.blaze.auth.http.AuthHeaderAuthenticator;
import com.nortal.blaze.auth.http.HttpAuthorization;
import org.apache.cxf.message.Message;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

@Component(immediate = true, service = AuthHeaderAuthenticator.class, configurationPid = "com.nortal.blaze.auth")
public class OidcAuthenticator implements AuthHeaderAuthenticator {
  private final ClientBuilder clientBuilder;
  private String oidcUrl;

  @Reference
  private ClientIdentity clientIdentity;

  public OidcAuthenticator() {
    this.clientBuilder = ClientBuilder.newBuilder();
  }

  @Activate
  @Modified
  void activate(Map<String, String> props) {
    oidcUrl = props.get("oidc.url");
  }

  @Override
  public User autheticate(HttpAuthorization auth, Message message) {
    if (!auth.isType(HttpAuthorization.BEARER)) {
      return null;
    }
    String token = auth.getCredential();
    if (token == null) {
      return null;
    }
    Builder request = clientBuilder.build().target(oidcUrl + "/profile").request();
    Response response = request.header("Authorization", "Bearer " + token).get();

    if (response.getStatus() >= 400) {
      return null;
    }

    Map<String, Object> userJson = fromJson(response.readEntity(String.class));
    User user = new User();
    
    Map<String, Object> claims = new HashMap<>(userJson);
    claims.remove("auth_time");
    user.setClaims(claims);
    return user;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> fromJson(String json) {
    return new Gson().fromJson(json, Map.class);
  }

}
