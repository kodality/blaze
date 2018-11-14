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
 package com.nortal.blaze.fhir.structure.util;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public final class ResourcePropertyUtilTest {

  @Test
  public void findReferences() {
    Composition c = new Composition();
    c.setSubject(new Reference().setReference("patient"));
    c.addEvent().addDetail().setReference("event");
    Bundle b = new Bundle();
    b.addEntry().setResource(c);
    List<Reference> refs = ResourcePropertyUtil.findProperties(b, Reference.class).collect(toList());
    Assert.assertEquals(2, refs.size());
    Set<String> refStrings = refs.stream().map(ref -> ref.getReference()).collect(toSet());
    Assert.assertTrue(refStrings.contains("event"));
    Assert.assertTrue(refStrings.contains("patient"));
  }

}
