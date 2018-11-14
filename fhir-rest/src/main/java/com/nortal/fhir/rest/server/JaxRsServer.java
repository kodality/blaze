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
 package com.nortal.fhir.rest.server;

import com.nortal.blaze.core.util.Osgi;
import com.nortal.fhir.rest.exception.FhirExceptionHandler;
import com.nortal.fhir.rest.filter.CharsetInterceptor;
import com.nortal.fhir.rest.filter.CleanupInterceptor;
import com.nortal.fhir.rest.filter.FormatInterceptor;
import com.nortal.fhir.rest.filter.InInterceptor;
import com.nortal.fhir.rest.filter.OutInterceptor;
import com.nortal.fhir.rest.filter.RequestContext;
import com.nortal.fhir.rest.filter.ResponseFormatInterceptor;
import com.nortal.fhir.rest.filter.writer.FhirWriter;
import com.nortal.fhir.rest.filter.writer.ResourceContentWriter;
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

import java.util.ArrayList;
import java.util.List;

public abstract class JaxRsServer {
  protected Server serverInstance;

  protected abstract String getEndpoint();

  protected abstract UserResource getResource();

  public Server createServer() {
    if (serverInstance != null) {
      return serverInstance;
    }
    JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
    sf.setAddress("/" + getEndpoint());
    sf.setProviders(getProviders());
    sf.setInInterceptors(getInInterceptors());
    sf.setOutInterceptors(getOutInterceptors());

    sf.setModelBeans(getResource());
    for (ClassResourceInfo cri : sf.getServiceFactory().getClassResourceInfo()) {
      cri.setResourceProvider(new SingletonResourceProvider(this));
    }

    this.serverInstance = sf.create();
    return this.serverInstance;
  }

  public Server getServerInstance() {
    return serverInstance;
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
    providers.add(new ResponseFormatInterceptor());
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
      beans.stream().filter(i -> StringUtils.equals(getPhase(), i.getPhase())).forEach(i -> i.handleMessage(message));
    }

  }

}
