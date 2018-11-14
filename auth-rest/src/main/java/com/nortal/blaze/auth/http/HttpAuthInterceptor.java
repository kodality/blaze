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
package com.nortal.blaze.auth.http;

import com.nortal.blaze.auth.ClientIdentity;
import com.nortal.blaze.auth.User;
import com.nortal.blaze.core.exception.FhirException;
import com.nortal.fhir.rest.filter.InInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component(immediate = true, service = InInterceptor.class)
@Slf4j
public class HttpAuthInterceptor extends InInterceptor {
  private static final String AUTHORIZATION = "Authorization";
  @Reference
  private ClientIdentity clientIdentity;
  @Reference(policy = ReferencePolicy.DYNAMIC)
  private final List<AuthHeaderAuthenticator> authenticators = new ArrayList<>();

  public HttpAuthInterceptor() {
    super(Phase.READ);
  }

  @Override
  public void handle(Message message) {
    HttpServletRequest request = (HttpServletRequest) message.get("HTTP.REQUEST");
    if ("/metadata".equals(request.getPathInfo())) {
      return;
    }

    if (clientIdentity.isAuthenticated()) {
      throw new IllegalStateException("context cleanup not worked, panic");
    }
    List<HttpAuthorization> auths = HttpAuthorization.parse(Collections.list(request.getHeaders(AUTHORIZATION)));

    User user =
        authenticators.stream().map(a -> a.autheticate(auths, message)).filter(a -> a != null).findFirst().orElse(null);
    clientIdentity.set(user);

    if (!clientIdentity.isAuthenticated()) {
      log.debug("could not authenticate. tried services: " + authenticators);
      throw new FhirException(401, IssueType.LOGIN, "not authenticated");
    }

  }

  protected void bind(AuthHeaderAuthenticator authenticator) {
    authenticators.add(authenticator);
  }

  protected void unbind(AuthHeaderAuthenticator authenticator) {
    authenticators.remove(authenticator);
  }

}
