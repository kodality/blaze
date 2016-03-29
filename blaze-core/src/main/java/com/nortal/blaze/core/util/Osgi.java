package com.nortal.blaze.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public final class Osgi {
  private Osgi() {
    // util
  }

  public static <T> T requireBean(Class<T> clazz) {
    List<T> beans = getBeans(clazz);
    if (CollectionUtils.isEmpty(beans) || beans.size() > 1) {
      throw new IllegalStateException("expecting single " + clazz.getName());
    }
    return beans.get(0);
  }

  public static <T> T getBean(Class<T> clazz) {
    List<T> beans = getBeans(clazz);
    return CollectionUtils.isEmpty(beans) ? null : beans.get(0);
  }

  public static <T> T getBean(Class<T> clazz, String filter) {
    BundleContext bc = FrameworkUtil.getBundle(Osgi.class).getBundleContext();
    List<T> beans = getBeans(clazz, filter, bc);
    return CollectionUtils.isEmpty(beans) ? null : beans.get(0);
  }

  public static <T> List<T> getBeans(Class<T> clazz) {
    BundleContext bc = FrameworkUtil.getBundle(Osgi.class).getBundleContext();
    return getBeans(clazz, null, bc);
  }

  public static <T> List<T> getBeans(Class<T> clazz, String filter, BundleContext bundleContext) {
    try {
      Collection<ServiceReference<T>> references = bundleContext.getServiceReferences(clazz, filter);
      if (references == null) {
        throw new RuntimeException(clazz.getName() + " not found");
      }
      List<T> result = new ArrayList<>();
      for (ServiceReference<T> reference : references) {
        result.add(bundleContext.getService(reference));
      }
      return result;
    } catch (InvalidSyntaxException e) {
      throw new RuntimeException(e);
    }
  }

}
