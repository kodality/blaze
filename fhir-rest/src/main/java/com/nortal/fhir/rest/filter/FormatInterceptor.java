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
 package com.nortal.fhir.rest.filter;

import com.nortal.blaze.core.exception.FhirException;
import com.nortal.blaze.core.exception.FhirServerException;
import com.nortal.blaze.fhir.structure.api.FhirContentType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueType;

import javax.ws.rs.core.MediaType;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

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
    String headerValue = getHeader(message, header);
    if (headerValue == null) {
      return;
    }
    List<String> newTypes = Stream.of(StringUtils.split(headerValue, ",")).map(value -> {
      MediaType mediaType = MediaType.valueOf(StringUtils.trim(value));
      if (mediaType.isWildcardSubtype() && mediaType.isWildcardType()) {
        return mediaType.toString();
      }
      String contentType = mediaType.getType() + "/" + mediaType.getSubtype();
      String charset = mediaType.getParameters().get(MediaType.CHARSET_PARAMETER);
      String newType = FhirContentType.getMimeType(contentType);
      if (newType == null) {
        return null;
      }
      if (charset != null) {
        newType = newType + ";" + MediaType.CHARSET_PARAMETER + "=" + charset;
      }
      return newType;
    }).distinct().filter(n -> n != null).collect(toList());
    if (CollectionUtils.isEmpty(newTypes)) {
      throw new FhirException(415, IssueType.NOTSUPPORTED, "format '" + headerValue + "' not supported");
    }
    setHeader(message, header, newTypes.get(0));
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
        setHeader(message, Message.CONTENT_TYPE, type);
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
      throw new FhirServerException(500, "there are two ways to write error-free programs");
    }
  }

}
