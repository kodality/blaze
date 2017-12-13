package com.nortal.blaze.auth.http;

import com.nortal.blaze.auth.ClientIdentity;
import com.nortal.fhir.rest.filter.OutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = OutInterceptor.class)
public class CleanupInterceptor extends OutInterceptor {
  @Reference
  private ClientIdentity clientIdentity;

  public CleanupInterceptor() {
    super(Phase.SETUP_ENDING);
  }

  @Override
  public void handle(Message message) {
    clientIdentity.remove();
  }

}
