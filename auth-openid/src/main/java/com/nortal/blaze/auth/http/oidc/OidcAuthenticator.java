/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.nortal.blaze.auth.http.oidc;

import com.google.gson.Gson;
import com.nortal.blaze.auth.ClientIdentity;
import com.nortal.blaze.auth.User;
import com.nortal.blaze.auth.http.AuthHeaderAuthenticator;
import com.nortal.blaze.auth.http.HttpAuthorization;
import com.nortal.blaze.core.exception.FhirException;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.message.Message;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Component(immediate = true, service = AuthHeaderAuthenticator.class, configurationPid = "com.nortal.blaze.auth.openid")
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
  public void activate(Map<String, String> props) {
    oidcUrl = props.get("oidc.url");
  }

  @Override
  public User autheticate(List<HttpAuthorization> auths, Message message) {
    String bearer = auths.stream().filter(a -> a.isType("Bearer")).findFirst().map(a -> a.getCredential()).orElse(null);
    if (bearer == null) {
      return null;
    }
    Map<String, Object> userJson = readProfile(bearer);
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

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private Set<String> getScopes(Map<String, Object> userJson) {
    Object scope = userJson.get("scope");
    if (scope instanceof String) {
      return Stream.of(StringUtils.split((String) scope, ";")).map(s -> s.trim()).collect(toSet());
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
    Builder request = clientBuilder.build().target(oidcUrl + "/profile").request();
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
