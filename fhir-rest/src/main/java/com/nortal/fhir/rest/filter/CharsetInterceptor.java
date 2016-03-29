package com.nortal.fhir.rest.filter;

import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.log4j.Logger;

public class CharsetInterceptor extends AbstractPhaseInterceptor<Message> {

  private static final String UTF_8 = "utf-8";
  private static final String CHARSET = "charset";

  public CharsetInterceptor() {
    super(Phase.SEND);
  }

  @Override
  public void handleMessage(Message message) throws Fault {
    try {
      if (!message.containsKey(Message.CONTENT_TYPE)) {
        return;
      }
      String header = (String) message.get(Message.CONTENT_TYPE);
      if (!hasCharset(header)) {
        String contentType = header + ";" + CHARSET + "=" + UTF_8;
        message.put(Message.CONTENT_TYPE, contentType);
        ((HttpServletResponse) message.get("HTTP.RESPONSE")).setHeader(Message.CONTENT_TYPE, contentType);
      }
    } catch (Exception e) {
      Logger.getLogger(CharsetInterceptor.class).error(e);
    }
  }

  private boolean hasCharset(String mime) {
    for (String part : StringUtils.split(mime, ';')) {
      if (part.startsWith(CHARSET + "=")) {
        return true;
      }
    }
    return false;
  }

}
