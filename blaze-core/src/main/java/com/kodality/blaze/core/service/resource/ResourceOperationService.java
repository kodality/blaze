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
 package com.kodality.blaze.core.service.resource;

import com.kodality.blaze.core.api.resource.InstanceOperationDefinition;
import com.kodality.blaze.core.api.resource.OperationInterceptor;
import com.kodality.blaze.core.api.resource.TypeOperationDefinition;
import com.kodality.blaze.core.exception.FhirException;
import com.kodality.blaze.core.model.ResourceId;
import com.kodality.blaze.fhir.structure.api.ResourceContent;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.List;

import static org.osgi.service.component.annotations.ReferenceCardinality.MULTIPLE;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;

@Component(immediate = true, service = ResourceOperationService.class)
public class ResourceOperationService {
  private final List<InstanceOperationDefinition> instanceOperations = new ArrayList<>();
  private final List<TypeOperationDefinition> typeOperations = new ArrayList<>();
  private final List<OperationInterceptor> interceptors = new ArrayList<>();

  public ResourceContent runInstanceOperation(String operation, ResourceId id, ResourceContent parameters) {
    interceptors.forEach(i -> i.handle("instance", operation, parameters));
    return instanceOperations.stream()
        .filter(op -> operation.equals("$" + op.getOperationName()))
        .filter(op -> op.getResourceType().equals(id.getResourceType()))
        .findFirst()
        .orElseThrow(() -> new FhirException(404, IssueType.NOTFOUND, "operation " + operation + " not found"))
        .run(id, parameters);
  }

  public ResourceContent runTypeOperation(String operation, String type, ResourceContent parameters) {
    interceptors.forEach(i -> i.handle("type", operation, parameters));
    return typeOperations.stream()
        .filter(op -> operation.equals("$" + op.getOperationName()))
        .filter(op -> op.getResourceType().equals(type))
        .findFirst()
        .orElseThrow(() -> new FhirException(404, IssueType.NOTFOUND, "operation " + operation + " not found"))
        .run(parameters);
  }

  @Reference(cardinality = MULTIPLE, policy = DYNAMIC, service = InstanceOperationDefinition.class, name = "InstanceOperationDefinition")
  protected void bind(InstanceOperationDefinition def) {
    this.instanceOperations.add(def);
  }

  protected void unbind(InstanceOperationDefinition def) {
    this.instanceOperations.remove(def);
  }

  @Reference(cardinality = MULTIPLE, policy = DYNAMIC, service = TypeOperationDefinition.class, name = "TypeOperationDefinition")
  protected void bind(TypeOperationDefinition def) {
    this.typeOperations.add(def);
  }

  protected void unbind(TypeOperationDefinition def) {
    this.typeOperations.remove(def);
  }

  @Reference(cardinality = MULTIPLE, policy = DYNAMIC, service = OperationInterceptor.class, name = "OperationInterceptor")
  protected void bind(OperationInterceptor interceptor) {
    this.interceptors.add(interceptor);
  }

  protected void unbind(OperationInterceptor interceptor) {
    this.interceptors.remove(interceptor);
  }
}
