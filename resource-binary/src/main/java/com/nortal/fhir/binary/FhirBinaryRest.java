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
package com.nortal.fhir.binary;

import com.nortal.blaze.core.exception.FhirException;
import com.nortal.fhir.rest.interaction.InteractionUtil;
import com.nortal.fhir.rest.server.FhirResourceRest;
import com.nortal.fhir.rest.server.FhirResourceServer;
import org.apache.cxf.jaxrs.model.UserResource;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public class FhirBinaryRest extends FhirResourceServer {

  public FhirBinaryRest(CapabilityStatementRestResourceComponent capability) {
    super(capability);
  }

  @Override
  protected UserResource getResource() {
    UserResource resource = new UserResource(this.getClass().getName(), "/");
    resource.setOperations(InteractionUtil.getOperations(capability, FhirResourceRest.class));
    return resource;
  }

  @Override
  public Response searchForm(MultivaluedMap<String, String> params) {
    throw new FhirException(400, IssueType.NOTSUPPORTED, "'Binary' search not supported");
  }

  // TODO: should be saved differently

}
