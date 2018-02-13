package com.nortal.blaze.store;

import com.nortal.blaze.core.api.transaction.TransactionManager;
import com.nortal.blaze.core.api.transaction.TransactionRef;
import com.nortal.blaze.util.sql.FhirDataSource;
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
  private PlatformTransactionManager tm;

  @Reference(name = "dataSource")
  protected void bind(FhirDataSource ds) {
    this.tm = new DataSourceTransactionManager(ds);
  }

  protected void unbind(FhirDataSource ds) {
    this.tm = null;
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

}
