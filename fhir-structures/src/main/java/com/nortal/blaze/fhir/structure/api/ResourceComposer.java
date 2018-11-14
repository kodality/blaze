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
 package com.nortal.blaze.fhir.structure.api;

import com.nortal.blaze.fhir.structure.service.ResourceFormatService;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Resource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

public class ResourceComposer {

  public static ResourceContent compose(Resource resource, String mime) {
    return getService().compose(resource, mime);
  }

  public static <R extends Resource> R parse(String input) {
    return getService().parse(input);
  }

  public static <R extends Resource> R parse(File file) {
    try {
      return parse(FileUtils.readFileToString(file, "UTF8"));
    } catch (IOException e) {
      throw new ParseException(e);
    }
  }

  private static ResourceFormatService getService() {
    try {
      Class<ResourceFormatService> needle = ResourceFormatService.class;
      BundleContext bc = FrameworkUtil.getBundle(ResourceComposer.class).getBundleContext();
      Stream<ResourceFormatService> services =
          bc.getServiceReferences(needle, null).stream().map(ref -> bc.getService(ref));
      return services.findFirst().orElseThrow(() -> new RuntimeException("ResourceRepresentationService not found"));
    } catch (InvalidSyntaxException e) {
      throw new RuntimeException(e);
    }
  }

}
