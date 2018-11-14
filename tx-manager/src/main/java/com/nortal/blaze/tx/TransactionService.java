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
 package com.nortal.blaze.tx;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

@Component(immediate = true, service = TransactionService.class)
public class TransactionService {
  @Reference(policy = ReferencePolicy.DYNAMIC)
  private final List<TransactionManager> txManagers = new ArrayList<>();

  public <T> T transaction(Supplier<T> fn) {
    List<TransactionRef> txs = txManagers.stream().map(tx -> tx.requireTransaction()).collect(toList());
    T returnme = null;
    try {
      returnme = fn.get();
    } catch (Throwable ex) {
      txs.forEach(tx -> tx.rollback(ex));
      throw ex;
    } finally {
      txs.forEach(tx -> tx.cleanupTransaction());
    }
    txs.forEach(tx -> tx.commit());
    return returnme;
  }

  protected void bind(TransactionManager txManager) {
    txManagers.add(txManager);
  }

  protected void unbind(TransactionManager txManager) {
    txManagers.remove(txManager);
  }

}
