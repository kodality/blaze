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
package com.kodality.blaze.auth.http.oidc;

import com.kodality.blaze.auth.User;
import com.kodality.blaze.auth.http.AuthHeaderAuthenticator;
import com.kodality.blaze.auth.http.HttpAuthorization;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.cxf.message.Message;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

//TODO: .well-known
@Component(immediate = true, service = {AuthHeaderAuthenticator.class})
public class OidcAuthenticator implements AuthHeaderAuthenticator {

  @Reference
  private OidcUserProvider oidcUserProvider;

  @Override
  public User autheticate(HttpServletRequest request, Message message) {
    List<HttpAuthorization> auths = HttpAuthorization.parse(Collections.list(request.getHeaders(AUTHORIZATION)));
    String bearerToken = auths.stream().filter(a -> a.isType("Bearer")).findFirst().map(HttpAuthorization::getCredential).orElse(null);
    return oidcUserProvider.getUser(bearerToken);
  }

}
