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
 package com.nortal.fhir.rest.command;

import com.nortal.fhir.rest.RestResourceInitializer;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "rest", name = "list", description = "show started rest services")
@Service
public class RestListCommand implements Action {
  @Reference
  private RestResourceInitializer restResourceInitializer;

  @Override
  public Object execute() throws Exception {
    restResourceInitializer.getServers().forEach((name, server) -> {
      String state = server.getServerInstance().isStarted() ? "STARTED" : "unknown";
      String path = server.getServerInstance().getDestination().getAddress().getAddress().getValue();
      System.out.println(String.format("%-26s", name) + " " + state + " " + path);
    });
    return null;
  }

}
