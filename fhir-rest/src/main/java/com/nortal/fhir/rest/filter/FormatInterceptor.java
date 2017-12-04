package com.nortal.fhir.rest.filter;

import com.nortal.blaze.fhir.structure.api.FhirContentType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class FormatInterceptor extends AbstractPhaseInterceptor<Message> {

  private static final String FORMAT = "_format";

  public FormatInterceptor() {
    super(Phase.READ);
  }

  @Override
  public void handleMessage(Message message) throws Fault {
    readUrlFormat(message);
    fixHeaderType(message, Message.CONTENT_TYPE);
    fixHeaderType(message, Message.ACCEPT_CONTENT_TYPE);
  }

  private void fixHeaderType(Message message, String header) {
    String value = getHeader(message, header);
    if (value == null || StringUtils.equals(value, MediaType.WILDCARD)) {
      return;
    }
    String type = FhirContentType.getMimeType(value);
    if (type != null) {
      setHeader(message, header, type);
    }
  }

  private void readUrlFormat(Message message) {
    String query = (String) message.get(Message.QUERY_STRING);
    if (StringUtils.isEmpty(query)) {
      return;
    }

    for (String param : query.split("&")) {
      if (StringUtils.startsWith(param, FORMAT)) {
        query = StringUtils.remove(query, param);
        message.put(Message.QUERY_STRING, query);

        String type = StringUtils.remove(param, FORMAT + "=");
        setHeader(message, Message.ACCEPT_CONTENT_TYPE, type);
        if (message.get(Message.CONTENT_TYPE) == null) {
          setHeader(message, Message.CONTENT_TYPE, type);
        }
        return;
      }
    }
  }

  @SuppressWarnings("unchecked")
  private String getHeader(Message message, String header) {
    Map<String, List<?>> headers = (Map<String, List<?>>) message.get(Message.PROTOCOL_HEADERS);
    if (headers.containsKey(header)) {
      return (String) headers.get(header).get(0);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private void setHeader(Message message, String header, Object value) {
    message.put(header, value);
    Map<String, List<?>> map = (Map<String, List<?>>) message.get(Message.PROTOCOL_HEADERS);
    map.put(header, Collections.singletonList(value));
    message.put(Message.PROTOCOL_HEADERS, map);
  }

}
