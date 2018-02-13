package com.nortal.fhir.rest.root;

import com.nortal.blaze.core.api.transaction.TransactionManager;
import com.nortal.blaze.core.api.transaction.TransactionRef;
import com.nortal.blaze.core.exception.FhirException;
import org.apache.commons.lang3.ObjectUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.Bundle.HTTPVerb;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

@Component(immediate = true, service = BundleService.class)
public class BundleService {
  @Reference
  private BundleReferenceHandler bundleReferenceHandler;
  @Reference
  private BundleEntryCxfProcessor bundleEntryProcessor;
  @Reference(policy = ReferencePolicy.DYNAMIC)
  private final List<TransactionManager> txManagers = new ArrayList<>();

  public Bundle save(Bundle bundle) {
    if (bundle.getEntry().stream().anyMatch(e -> !e.hasRequest())) {
      throw new FhirException(400, "request element required");
    }
    bundleReferenceHandler.replaceIds(bundle);
    bundle.getEntry().sort(new EntityMethodOrderComparator());

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
        throw new RuntimeException("entry: " + entry.getFullUrl(), e.getCause());
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
    newEntry.addLink().setRelation("alternate").setUrl(entry.getFullUrl());
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

  private static class EntityMethodOrderComparator implements Comparator<BundleEntryComponent> {
    private static final List<HTTPVerb> order =
        Arrays.asList(HTTPVerb.DELETE, HTTPVerb.POST, HTTPVerb.PUT, HTTPVerb.GET);

    @Override
    public int compare(BundleEntryComponent o1, BundleEntryComponent o2) {
      return ObjectUtils.compare(order.indexOf(o1.getRequest().getMethod()),
                                 order.indexOf(o2.getRequest().getMethod()));
    }

  }

}
