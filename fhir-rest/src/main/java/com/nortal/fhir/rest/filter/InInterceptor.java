package com.nortal.fhir.rest.filter;

import com.nortal.fhir.rest.exception.FhirExceptionHandler;
import javax.ws.rs.core.Response;
import org.apache.cxf.interceptor.ServiceInvokerInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class InInterceptor extends AbstractPhaseInterceptor<Message> {
  private static final Logger LOG = LogManager.getLogger(InInterceptor.class);
  private boolean pohuist;

  public InInterceptor(String phase) {
    super(phase);
  }

  public void setPohuist(boolean pohuist) {
    this.pohuist = pohuist;
  }

  public abstract void handle(Message message);

  @Override
  public void handleMessage(Message message) {
    try {
      handle(message);
    } catch (Throwable e) {
      if (pohuist) {
        LOG.error(e);
      } else {
        message.getExchange().put(Response.class, FhirExceptionHandler.getResponse(e));
        message.getInterceptorChain().doInterceptStartingAt(message, ServiceInvokerInterceptor.class.getName());
        message.getInterceptorChain().abort();
      }
    }
  }

}
