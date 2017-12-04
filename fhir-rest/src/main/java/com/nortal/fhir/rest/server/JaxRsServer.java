package com.nortal.fhir.rest.server;

import com.nortal.blaze.core.util.Osgi;
import com.nortal.fhir.rest.exception.FhirExceptionHandler;
import com.nortal.fhir.rest.filter.CharsetInterceptor;
import com.nortal.fhir.rest.filter.CleanupInterceptor;
import com.nortal.fhir.rest.filter.FormatInterceptor;
import com.nortal.fhir.rest.filter.InInterceptor;
import com.nortal.fhir.rest.filter.OutInterceptor;
import com.nortal.fhir.rest.filter.RequestContext;
import com.nortal.fhir.rest.filter.writer.FhirWriter;
import com.nortal.fhir.rest.filter.writer.ResourceContentWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.UserResource;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public abstract class JaxRsServer {

  protected abstract String getEndpoint();

  protected abstract UserResource getResource();

  public Server createServer() {
    JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
    sf.setAddress("/" + getEndpoint());
    sf.setProviders(getProviders());
    sf.setInInterceptors(getInInterceptors());
    sf.setOutInterceptors(getOutInterceptors());

    sf.setModelBeans(getResource());
    for (ClassResourceInfo cri : sf.getServiceFactory().getClassResourceInfo()) {
      cri.setResourceProvider(new SingletonResourceProvider(this));
    }

    return sf.create();
  }

  private List<Interceptor<? extends Message>> getInInterceptors() {
    List<Interceptor<? extends Message>> interceptors = new ArrayList<>();
    interceptors.add(new FormatInterceptor());
    interceptors.add(new OsgiInterceptorProxy(Phase.RECEIVE, InInterceptor.class));
    interceptors.add(new OsgiInterceptorProxy(Phase.READ, InInterceptor.class));
    interceptors.add(new OsgiInterceptorProxy(Phase.PRE_INVOKE, InInterceptor.class));
    return interceptors;
  }

  private List<Interceptor<? extends Message>> getOutInterceptors() {
    List<Interceptor<? extends Message>> interceptors = new ArrayList<>();
    interceptors.add(new CharsetInterceptor());
    interceptors.add(new CleanupInterceptor());
    interceptors.add(new OsgiInterceptorProxy(Phase.PRE_STREAM, OutInterceptor.class));
    interceptors.add(new OsgiInterceptorProxy(Phase.SEND, OutInterceptor.class));
    interceptors.add(new OsgiInterceptorProxy(Phase.SETUP_ENDING, OutInterceptor.class));
    return interceptors;
  }

  private List<Object> getProviders() {
    List<Object> providers = new ArrayList<>();
    providers.add(new RequestContext());
    providers.add(new FhirExceptionHandler());
    providers.add(new ResourceContentWriter());
    providers.add(new FhirWriter());
    return providers;
  }

  private static class OsgiInterceptorProxy extends AbstractPhaseInterceptor<Message> {
    private final Class<? extends AbstractPhaseInterceptor<Message>> interceptorClass;

    public OsgiInterceptorProxy(String phase, Class<? extends AbstractPhaseInterceptor<Message>> interceptorClass) {
      super(phase);
      this.interceptorClass = interceptorClass;
    }

    @Override
    public void handleMessage(Message message) throws Fault {
      List<? extends AbstractPhaseInterceptor<Message>> beans = Osgi.getBeans(interceptorClass);
      if (CollectionUtils.isEmpty(beans)) {
        return;
      }
      for (AbstractPhaseInterceptor<Message> interceptor : beans) {
        if (!StringUtils.equals(getPhase(), interceptor.getPhase())) {
          continue;
        }
        interceptor.handleMessage(message);
      }
    }

  }

}
