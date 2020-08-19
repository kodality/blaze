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
 package com.kodality.blaze.core.util;

import org.apache.commons.collections4.CollectionUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

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
      if (bundleContext == null) {
        throw new RuntimeException("bundle not started yet?");
      }
      Collection<ServiceReference<T>> references = bundleContext.getServiceReferences(clazz, filter);
      if (references == null) {
        throw new RuntimeException(clazz.getName() + " not found");
      }
      return references.stream().map(ref -> bundleContext.getService(ref)).filter(s -> s != null).collect(toList());
    } catch (InvalidSyntaxException e) {
      throw new RuntimeException(e);
    }
  }

}
