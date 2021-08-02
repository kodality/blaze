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

import com.kodality.fhir.rest.exception.FhirExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.interceptor.ServiceInvokerInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

import javax.ws.rs.core.Response;

@Slf4j
public abstract class InInterceptor extends AbstractPhaseInterceptor<Message> {
  private boolean logAndIgnoreOnError;

  public InInterceptor(String phase) {
    super(phase);
  }

  public void setLogAndIgnoreOnError(boolean logAndIgnoreOnError) {
    this.logAndIgnoreOnError = logAndIgnoreOnError;
  }

  public abstract void handle(Message message);

  @Override
  public void handleMessage(Message message) {
    try {
      handle(message);
    } catch (Throwable e) {
      if (logAndIgnoreOnError) {
        log.error("", e);
      } else {
        message.getExchange().put(Response.class, FhirExceptionHandler.getResponse(e));
        message.getInterceptorChain().doInterceptStartingAt(message, ServiceInvokerInterceptor.class.getName());
        message.getInterceptorChain().abort();
      }
    }
  }

}
