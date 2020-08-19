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
package com.kodality.fhir.binary;

import com.kodality.fhir.rest.server.FhirResourceServerFactory;
import com.kodality.fhir.rest.server.JaxRsServer;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.ResourceType;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = FhirResourceServerFactory.class)
public class BinaryServerFactory implements FhirResourceServerFactory {

  @Override
  public String getType() {
    return ResourceType.Binary.name();
  }

  @Override
  public JaxRsServer construct(CapabilityStatementRestResourceComponent capability) {
    return new FhirBinaryRest(capability);
  }

}
