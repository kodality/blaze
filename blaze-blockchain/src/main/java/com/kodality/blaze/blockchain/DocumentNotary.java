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
 package com.kodality.blaze.blockchain;

import com.kodality.blaze.core.api.resource.ResourceAfterSaveInterceptor;
import com.kodality.blaze.core.model.ResourceVersion;
import com.kodality.blaze.core.util.JsonUtil;
import com.kodality.blaze.fhir.structure.service.ResourceFormatService;
import org.hl7.fhir.r4.model.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component(immediate = true, service = {ResourceAfterSaveInterceptor.class, DocumentNotary.class}, configurationPid = "com.kodality.blaze.blockchain")
public class DocumentNotary extends ResourceAfterSaveInterceptor {

    private String gwEndpoint;
    private final Client client;

    @Reference
    private ResourceFormatService formatService;

    @Activate
    @Modified
    public void activate(Map<String, String> props) {
        gwEndpoint = props.get("gateway.endpoint");
    }

    public DocumentNotary() {
        super(ResourceAfterSaveInterceptor.FINALIZATION);
        client = ClientBuilder.newBuilder().build();
    }


    public String checkDocument(ResourceVersion version) {
        Response response = post("/checkDocument", new DocumentPayload(version.getReference(), toJson(version)));
        return response.readEntity(String.class);
    }

    private Response post(String path, Object payload) {
        Builder req = client.target(gwEndpoint).path(path).request();
        return req.post(Entity.json(JsonUtil.toJson(payload)));
    }

    @Override
    public void handle(ResourceVersion version) {
      //TODO: queue
      CompletableFuture.runAsync(() -> {
        Response response = post("/notarize", new DocumentPayload(version.getReference(), toJson(version)));
        String hash = response.readEntity(String.class);
        //TODO: check if hash needs to be stored in db
        //TODO: log success/fail
      });
    }

    private String toJson(ResourceVersion version) {
        Resource parse = formatService.parse(version.getContent().getValue());
        parse.setMeta(null);
        parse.setId((String) null); //TODO: change
        return formatService.compose(parse, "json").getValue();
    }
}
