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
 package com.kodality.blaze.util.sql;

import com.kodality.blaze.tx.TransactionManager;
import com.kodality.blaze.tx.TransactionRef;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionAttribute;

import java.util.function.Supplier;

@Component(immediate = true, service = { PgTransactionManager.class, TransactionManager.class })
public class PgTransactionManager extends TransactionAspectSupport implements TransactionManager {
  protected PlatformTransactionManager tm;

  @Reference(name = "dataSource")
  protected void bind(FhirDataSource ds) {
    this.tm = new DataSourceTransactionManager(ds);
  }

  protected void unbind(FhirDataSource ds) {
    this.tm = null;
  }

  public void transaction(Runnable fn) {
    transaction(new DefaultTransactionAttribute(), () -> {
      fn.run();
      return null;
    });
  }

  public <T> T transaction(Supplier<T> fn) {
    return transaction(new DefaultTransactionAttribute(), fn);
  }

  public <T> T transaction(TransactionAttribute txAttr, Supplier<T> fn) {
    TransactionInfo txInfo = createTransactionIfNecessary(tm, txAttr, null);
    T retVal = null;
    try {
      retVal = fn.get();
    } catch (Throwable ex) {
      completeTransactionAfterThrowing(txInfo, ex);
      throw ex;
    } finally {
      cleanupTransactionInfo(txInfo);
    }
    commitTransactionAfterReturning(txInfo);
    return retVal;
  }

  @Override
  public TransactionRef requireTransaction() {
    TransactionAttribute txAttr = new DefaultTransactionAttribute();
    TransactionInfo txInfo = createTransactionIfNecessary(tm, txAttr, null);
    return new TransactionRef() {

      @Override
      public void rollback(Throwable e) {
        completeTransactionAfterThrowing(txInfo, e);
      }

      @Override
      public void commit() {
        commitTransactionAfterReturning(txInfo);
      }

      @Override
      public void cleanupTransaction() {
        cleanupTransactionInfo(txInfo);
      }
    };
  }

}
