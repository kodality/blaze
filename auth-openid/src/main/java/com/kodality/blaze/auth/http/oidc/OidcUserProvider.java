package com.kodality.blaze.auth.http.oidc;

import com.google.gson.Gson;
import com.kodality.blaze.auth.ClientIdentity;
import com.kodality.blaze.auth.User;
import com.kodality.blaze.core.exception.FhirException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import static java.util.stream.Collectors.toSet;

@Component(immediate = true, service = OidcUserProvider.class, configurationPid = "com.kodality.blaze.auth.openid")
public class OidcUserProvider {

  private final ClientBuilder clientBuilder;
  private String oidcUrl;

  private ClientIdentity clientIdentity;

  public OidcUserProvider() {
    this.clientBuilder = ClientBuilder.newBuilder();
  }

  @Activate
  @Modified
  public void activate(Map<String, String> props) {
    oidcUrl = props.get("oidc.url");
  }

  public User getUser(String bearerToken) {
    if (bearerToken == null) {
      return null;
    }
    Map<String, Object> userJson = readProfile(bearerToken);
    if (userJson == null) {
      return null;
    }

    User user = new User();
    user.setScopes(getScopes(userJson));

    Map<String, Object> claims = new HashMap<>(userJson);
    claims.remove("auth_time");
    claims.remove("scope");
    user.setClaims(claims);
    return user;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private Set<String> getScopes(Map<String, Object> userJson) {
    Object scope = userJson.get("scope");
    if (scope instanceof String) {
      return Stream.of(StringUtils.split((String) scope, ";")).map(String::trim).collect(toSet());
    }
    if (scope instanceof List) {
      return new HashSet<>((List) scope);
    }
    return null;
  }

  private Map<String, Object> readProfile(String bearer) {
    if (StringUtils.isEmpty(oidcUrl)) {
      throw new FhirException(500, IssueType.SECURITY, "server oidc config missing");
    }
    Builder request = clientBuilder.build().target(oidcUrl + "/userinfo").request();
    Response response = request.header("Authorization", "Bearer " + bearer).get();
    if (response.getStatus() >= 400) {
      return null;
    }
    return fromJson(response.readEntity(String.class));
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> fromJson(String json) {
    return new Gson().fromJson(json, Map.class);
  }
}
