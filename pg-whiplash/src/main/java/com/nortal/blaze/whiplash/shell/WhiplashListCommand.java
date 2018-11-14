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
 package com.nortal.blaze.whiplash.shell;

import com.nortal.blaze.whiplash.api.WhiplashRunner;
import org.apache.commons.lang3.StringUtils;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

@Command(scope = "whiplash", name = "list", description = "list propagation providing bundles")
@Service
public class WhiplashListCommand implements Action {
  @Reference
  private BundleContext bundleContext;

  @Override
  public Object execute() throws Exception {
    List<String> names = new ArrayList<>();
    bundleContext.getServiceReferences(WhiplashRunner.class,
                                       null).forEach(r -> names.add(bundleContext.getService(r).getName()));
    System.out.println(StringUtils.join(names, " "));
    return null;
  }

}
