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
 package com.kodality.fhir.rest.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import javax.servlet.http.HttpServletResponse;

@Slf4j
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
      if (header != null && !hasCharset(header)) {
        String contentType = header + ";" + CHARSET + "=" + UTF_8;
        message.put(Message.CONTENT_TYPE, contentType);
        ((HttpServletResponse) message.get("HTTP.RESPONSE")).setHeader(Message.CONTENT_TYPE, contentType);
      }
    } catch (Exception e) {
      log.error("", e);
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
