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
 package com.nortal.blaze.auth.smart;

import com.nortal.blaze.auth.ClientIdentity;
import com.nortal.blaze.core.exception.FhirException;
import com.nortal.fhir.rest.filter.InInterceptor;
import com.nortal.fhir.rest.interaction.InteractionUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nortal.blaze.core.model.InteractionType.CREATE;
import static com.nortal.blaze.core.model.InteractionType.DELETE;
import static com.nortal.blaze.core.model.InteractionType.HISTORYINSTANCE;
import static com.nortal.blaze.core.model.InteractionType.HISTORYTYPE;
import static com.nortal.blaze.core.model.InteractionType.OPERATION;
import static com.nortal.blaze.core.model.InteractionType.READ;
import static com.nortal.blaze.core.model.InteractionType.SEARCHTYPE;
import static com.nortal.blaze.core.model.InteractionType.UPDATE;
import static com.nortal.blaze.core.model.InteractionType.VALIDATE;
import static com.nortal.blaze.core.model.InteractionType.VREAD;
import static java.util.Arrays.asList;

@Component(immediate = true, service = { SmartAuthHttpInterceptor.class, InInterceptor.class })
public class SmartAuthHttpInterceptor extends InInterceptor {
  private static final Map<String, Map<String, List<String>>> contexts = new HashMap<>();
  static {
    HashMap<String, List<String>> patient = new HashMap<>();
    patient.put(Right.read, asList(READ, VREAD, HISTORYINSTANCE));
    patient.put(Right.write, asList(UPDATE, DELETE, OPERATION));
    patient.put(Right.all, asList(READ, VREAD, HISTORYINSTANCE, UPDATE, DELETE, OPERATION));
    contexts.put(Context.patient, patient);

    HashMap<String, List<String>> user = new HashMap<>();
    user.put(Right.read, asList(READ, VREAD, HISTORYINSTANCE, SEARCHTYPE, HISTORYTYPE));
    user.put(Right.write, asList(UPDATE, DELETE, CREATE, VALIDATE, OPERATION));
    user.put(Right.all,
             asList(READ,
                    VREAD,
                    HISTORYINSTANCE,
                    SEARCHTYPE,
                    HISTORYTYPE,
                    UPDATE,
                    DELETE,
                    CREATE,
                    VALIDATE,
                    OPERATION));
    contexts.put(Context.user, user);
  }

  @Reference
  private ClientIdentity clientIdentity;

  public SmartAuthHttpInterceptor() {
    super(Phase.PRE_INVOKE);
  }

  @Override
  public void handle(Message message) {
    Method method = (Method) message.get("org.apache.cxf.resource.method");
    String interaction = InteractionUtil.getMethodInteraction(method);
    String resourceType =
        StringUtils.substringAfterLast((String) message.get("org.apache.cxf.message.Message.BASE_PATH"), "/");
    if ("".equals(resourceType)) {
      return; // transaction request. @see #SmartBundleEntryListener.beforeEntryInvoke
    }

    validate(interaction, resourceType);
  }

  public void validate(String interaction, String resourceType) {
    if (!clientIdentity.isAuthenticated()) {
      return; //TODO think
    }

    Set<String> clientScopes = clientIdentity.get().getScopes();
    if (clientScopes == null || clientScopes.stream()
        .map(s -> new SmartScope(s))
        .noneMatch(s -> isScopeAllowed(s, resourceType, interaction))) {
      throw new FhirException(403, IssueType.FORBIDDEN, resourceType + "." + interaction + " not allowed");
    }
  }

  private boolean isScopeAllowed(SmartScope scope, String resourceType, String interaction) {
    boolean resourceTypeAllowed = scope.getResourceType().equals("*") || scope.getResourceType().equals(resourceType);
    if (!resourceTypeAllowed) {
      return false;
    }
    if (!contexts.containsKey(scope.getContext())) {
      throw new FhirException(403, IssueType.FORBIDDEN, "unknown context");
    }
    Map<String, List<String>> contextPermissions = contexts.get(scope.getContext());
    if (!contextPermissions.containsKey(scope.getRights())) {
      throw new FhirException(403, IssueType.FORBIDDEN, "unknown right");
    }

    return contextPermissions.get(scope.getRights()).contains(interaction);
  }

}
