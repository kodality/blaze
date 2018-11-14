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
 package com.nortal.fhir.rest.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

@Slf4j
public abstract class OutInterceptor extends AbstractPhaseInterceptor<Message> {

  public OutInterceptor(String phase) {
    super(phase);
  }

  public abstract void handle(Message message);

  @Override
  public void handleMessage(Message message) {
    try {
      handle(message);
    } catch (Throwable e) {
      log.error("", e);
    }
  }

}
