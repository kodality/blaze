package com.nortal.fhir.rest.filter;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class CleanupInterceptor extends AbstractPhaseInterceptor<Message> {

  public CleanupInterceptor() {
    super(Phase.SETUP_ENDING);
  }

  @Override
  public void handleMessage(Message message) throws Fault {
    RequestContext.clear();
  }
}
