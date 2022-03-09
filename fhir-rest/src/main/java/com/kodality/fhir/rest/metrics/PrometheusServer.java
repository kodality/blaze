package com.kodality.fhir.rest.metrics;

import io.prometheus.client.exporter.common.TextFormat;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.UserOperation;
import org.apache.cxf.jaxrs.model.UserResource;
import org.apache.cxf.jaxrs.utils.AnnotationUtils;

public abstract class PrometheusServer {
  private Server serverInstance;

  public void start() {
    if (serverInstance == null) {
      serverInstance = createServer();
    }
  }

  public void stop() {
    if (serverInstance != null && serverInstance.isStarted()) {
      serverInstance.destroy();
    }
  }

  private Server createServer() {
    if (serverInstance != null) {
      return serverInstance;
    }
    JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
    sf.getServiceFactory().setDefaultModelClass(this.getClass());

    sf.setAddress("/prometheus");
    sf.setModelBeans(getResource());
    for (ClassResourceInfo cri : sf.getServiceFactory().getClassResourceInfo()) {
      cri.setResourceProvider(new SingletonResourceProvider(this));
    }

    this.serverInstance = sf.create();
    return this.serverInstance;
  }

  private UserResource getResource() {
    UserResource resource = new UserResource(this.getClass().getName(), "/");
    resource.setConsumes("*/*");
    resource.setProduces(TextFormat.CONTENT_TYPE_004);
    List<UserOperation> ops = getOperations();
    resource.setOperations(ops);
    return resource;
  }

  private List<UserOperation> getOperations() {
    List<UserOperation> ops = new ArrayList<>();
    List<Method> methods = Arrays.asList(PrometheusRest.class.getMethods());
    methods.forEach(m -> {
      Path path = m.getAnnotation(Path.class);
      UserOperation op = new UserOperation(m.getName(), path == null ? "" : path.value());
      String httpMethodValue = AnnotationUtils.getHttpMethodValue(m);
      op.setVerb(httpMethodValue);
      Consumes cm = AnnotationUtils.getMethodAnnotation(m, Consumes.class);
      if (cm != null) {
        String consumes = StringUtils.join(cm.value(), ",");
        op.setConsumes(consumes);
      }
      setProduces(m, op);
      ops.add(op);
    });
    return ops;
  }

  private void setProduces(Method m, UserOperation op) {
    Produces produces = AnnotationUtils.getMethodAnnotation(m, Produces.class);
    if (produces == null) {
      return;
    }
    String producesStr = StringUtils.join(produces.value(), ",");
    op.setConsumes(producesStr);
  }

}
