package com.nortal.fhir.rest.root;

import com.nortal.blaze.core.api.TransactionManager;
import com.nortal.blaze.core.api.TransactionRef;
import com.nortal.blaze.core.exception.FhirException;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

@Component(immediate = true, service = BundleService.class)
public class BundleService {
  @Reference
  private BundleEntryCxfProcessor bundleEntryProcessor;
  @Reference(policy = ReferencePolicy.DYNAMIC)
  private final List<TransactionManager> txManagers = new ArrayList<>();

  public Bundle save(Bundle bundle) {
    if (bundle.getType() == BundleType.BATCH) {
      return batch(bundle);
    }
    if (bundle.getType() == BundleType.TRANSACTION) {
      return transaction(bundle);
    }
    throw new FhirException(400, "only batch or transaction supported");
  }

  private Bundle batch(Bundle bundle) {
    Bundle responseBundle = new Bundle();
    bundle.getEntry().forEach(entry -> {
      try {
        responseBundle.addEntry(perform(entry));
      } catch (Exception e) {
        FhirException fhirException = findFhirException(e);
        if (fhirException != null) {
          BundleEntryResponseComponent responseEntry = new BundleEntryResponseComponent();
          responseEntry.setStatus("" + fhirException.getStatusCode());
          OperationOutcome outcome = new OperationOutcome();
          outcome.setIssue(fhirException.getIssues());
          responseEntry.setOutcome(outcome);
          responseBundle.addEntry().setResponse(responseEntry);
          return;
        }
        throw new RuntimeException("entry id: " + entry.getId(), e.getCause());
      }
    });
    responseBundle.setType(BundleType.BATCHRESPONSE);
    return responseBundle;
  }

  private Bundle transaction(Bundle bundle) {
    return transaction(() -> {
      Bundle responseBundle = new Bundle();
      bundle.getEntry().forEach(entry -> responseBundle.addEntry(perform(entry)));
      responseBundle.setType(BundleType.TRANSACTIONRESPONSE);
      return responseBundle;
    });
  }

  private BundleEntryComponent perform(BundleEntryComponent entry) {
    BundleEntryResponseComponent responseEntry = bundleEntryProcessor.perform(entry);
    BundleEntryComponent newEntry = new BundleEntryComponent();
    newEntry.setResponse(responseEntry);
    newEntry.addLink().setRelation("alternate").setUrl(entry.getId());
    return newEntry;
  }

  private <T> T transaction(Supplier<T> fn) {
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

  private FhirException findFhirException(Throwable e) {
    if (e instanceof FhirException) {
      return (FhirException) e;
    }
    if (e.getCause() != null) {
      return findFhirException(e.getCause());
    }
    return null;
  }

}
