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
package com.kodality.blaze.auth.http.yupi;

import com.kodality.blaze.auth.User;
import com.kodality.blaze.auth.http.AuthHeaderAuthenticator;
import com.kodality.blaze.auth.http.HttpAuthorization;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.apache.cxf.message.Message;
import org.osgi.service.component.annotations.Component;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

@Component(immediate = true, service = AuthHeaderAuthenticator.class)
public class YupiAuthenticator implements AuthHeaderAuthenticator {
  private static final Map<String, String> yupiOrgs = new HashMap<>();

  static {
    yupiOrgs.put("yupi", "yupland");
    yupiOrgs.put("ipuy", "dnalpuy");
  }

  @Override
  public User autheticate(HttpServletRequest request, Message message) {
    List<HttpAuthorization> auths = HttpAuthorization.parse(Collections.list(request.getHeaders(AUTHORIZATION)));
    return auths.stream()
        .filter(a -> a.isType("Bearer"))
        .filter(bearer -> yupiOrgs.containsKey(bearer.getCredential()))
        .map(bearer -> makeYupi(bearer.getCredential(), yupiOrgs.get(bearer.getCredential())))
        .map(user -> decorateClaims(user, getClaimHeaders(request))).findFirst().orElse(null);
  }

  private Map<String, String> getClaimHeaders(HttpServletRequest request) {
    Map<String, String> claims = new HashMap<>();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      if (headerName.startsWith("x-claim-")) {
        String claim = headerName.replace("x-claim-", "");
        claims.put(claim, request.getHeader(headerName));
      }
    }
    return claims;
  }

  private User decorateClaims(User user, Map<String, String> claimHeaders) {
    claimHeaders.forEach((k, v) -> user.getClaims().put(k, v));
    return user;
  }

  private static User makeYupi(String sub, String org) {
    User user = new User();
    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", sub);
    claims.put("org", org);
    user.setClaims(claims);
    user.setScopes(Collections.singleton("user/*.*"));
    return user;
  }

}
