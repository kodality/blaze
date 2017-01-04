package com.nortal.fhir.rest.filter;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class OutInterceptor extends AbstractPhaseInterceptor<Message> {
  private static final Logger LOG = LogManager.getLogger(OutInterceptor.class);

  public OutInterceptor(String phase) {
    super(phase);
  }

  public abstract void handle(Message message);

  @Override
  public void handleMessage(Message message) {
    try {
      handle(message);
    } catch (Throwable e) {
      LOG.error(e);
    }
  }

}
