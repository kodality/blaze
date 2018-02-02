package com.nortal.blaze.core.api;

public interface TransactionRef {

  void rollback(Throwable e);

  void commit();

  void cleanupTransaction();
}
