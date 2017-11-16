package com.nortal.blaze.fhir.structure.api;

import com.nortal.blaze.fhir.structure.service.ResourceRepresentationService;
import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Resource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

public class ResourceComposer {

  public static String compose(Resource resource, String mime) {
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

  private static ResourceRepresentationService getService() {
    try {
      Class<ResourceRepresentationService> needle = ResourceRepresentationService.class;
      BundleContext bc = FrameworkUtil.getBundle(ResourceComposer.class).getBundleContext();
      Stream<ResourceRepresentationService> services =
          bc.getServiceReferences(needle, null).stream().map(ref -> bc.getService(ref));
      return services.findFirst().orElseThrow(() -> new RuntimeException("ResourceRepresentationService not found"));
    } catch (InvalidSyntaxException e) {
      throw new RuntimeException(e);
    }
  }

}
