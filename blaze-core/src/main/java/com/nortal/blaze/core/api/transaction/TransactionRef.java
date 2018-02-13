package com.nortal.blaze.core.api.transaction;

public interface TransactionRef {

  void rollback(Throwable e);

  void commit();

  void cleanupTransaction();
}
