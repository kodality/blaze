package com.nortal.blaze.core.util;

import com.nortal.blaze.core.model.VersionId;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public final class ResourceUtil {

  private static final String HISTORY = "_history";

  private ResourceUtil() {
    // no init
  }

  public static VersionId parseReference(String uri) {
    if (StringUtils.isEmpty(uri) || uri.startsWith("#")) {
      return null;
    }
    String[] tokens = StringUtils.split(uri, "/");
    VersionId id = new VersionId(tokens[0]);
    if (tokens.length > 1) {
      id.setResourceId(tokens[1]);
    }
    if (tokens.length > 3) {
      Validate.isTrue(HISTORY.equals(tokens[3]));
      id.setVersion(Integer.valueOf(tokens[3]));
    }
    return id;
  }

}
