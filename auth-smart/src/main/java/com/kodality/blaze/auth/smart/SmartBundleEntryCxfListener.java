package com.kodality.blaze.auth.smart;

import com.kodality.fhir.rest.interaction.InteractionUtil;
import com.kodality.fhir.rest.root.BundleEntryCxfListener;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.lang.reflect.Method;
import java.net.URI;

@Component(immediate = true, service = BundleEntryCxfListener.class)
public class SmartBundleEntryCxfListener implements BundleEntryCxfListener {
  @Reference
  private SmartAuthHttpInterceptor smart;

  @Override
  public void beforeInvoke(Method method, URI uri) {
    String interaction = InteractionUtil.getMethodInteraction(method);
    String resourceType = StringUtils.substringBefore(uri.getPath(), "/");
    smart.validate(interaction, resourceType);
  }

}
