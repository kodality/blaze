package com.nortal.blaze.representation.defs;

import com.nortal.blaze.representation.defs.json.JsonComposer;
import com.nortal.blaze.representation.defs.json.JsonParser;
import com.nortal.blaze.representation.defs.xml.XmlComposer;
import com.nortal.blaze.representation.defs.xml.XmlParser;
import java.util.HashMap;
import java.util.Map;

public final class ResourceRepresentation {
  private ResourceRepresentation() {
    //
  }

  private static final Map<RepresentationType, ResourceRepresentationComposer> composers;
  private static final Map<RepresentationType, ResourceRepresentationParser> parsers;

  static {
    composers = new HashMap<>();
    composers.put(RepresentationType.XML, new XmlComposer());
    composers.put(RepresentationType.JSON, new JsonComposer());

    parsers = new HashMap<>();
    parsers.put(RepresentationType.XML, new XmlParser());
    parsers.put(RepresentationType.JSON, new JsonParser());
  }

  public static ResourceRepresentationComposer getComposer(String mimeType) {
    for (RepresentationType type : composers.keySet()) {
      if (type.getMimeTypes().contains(mimeType)) {
        return composers.get(type);
      }
    }
    return null;
  }

  public static ResourceRepresentationParser getParser(String content) {
    for (ResourceRepresentationParser parser : parsers.values()) {
      if (parser.isParsable(content)) {
        return parser;
      }
    }
    return null;
  }

}
