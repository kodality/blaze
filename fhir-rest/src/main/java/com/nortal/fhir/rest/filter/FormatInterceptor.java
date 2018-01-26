package com.nortal.fhir.rest.filter;

import com.nortal.blaze.core.exception.FhirException;
import com.nortal.blaze.core.exception.ServerException;
import com.nortal.blaze.fhir.structure.api.FhirContentType;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import javax.ws.rs.core.MediaType;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    if (value == null) {
      return;
    }
    MediaType mediaType = MediaType.valueOf(value);
    if (mediaType.isWildcardSubtype() && mediaType.isWildcardType()) {
      return;
    }
    String type = mediaType.getType() + "/" + mediaType.getSubtype();
    String charset = mediaType.getParameters().get(MediaType.CHARSET_PARAMETER);
    String newType = FhirContentType.getMimeType(type);
    if (newType == null) {
      throw new FhirException(415, value + " not supported");
    }
    if (charset != null) {
      newType = newType + ";" + MediaType.CHARSET_PARAMETER + "=" + charset;
    }
    setHeader(message, header, newType);
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

        String type = decode(StringUtils.remove(param, FORMAT + "="));
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

  private static String decode(String s) {
    try {
      return URLDecoder.decode(s, "UTF8");
    } catch (UnsupportedEncodingException e) {
      throw new ServerException("there are two ways to write error-free programs");
    }
  }

}
