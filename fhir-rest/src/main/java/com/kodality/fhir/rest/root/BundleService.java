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
package com.kodality.fhir.rest.root;

import com.kodality.blaze.core.exception.FhirException;
import com.kodality.blaze.core.model.search.SearchCriterion;
import com.kodality.blaze.core.model.search.SearchResult;
import com.kodality.blaze.core.service.resource.ResourceSearchService;
import com.kodality.blaze.core.service.resource.SearchUtil;
import com.kodality.blaze.tx.TransactionService;
import org.apache.commons.lang3.ObjectUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component(immediate = true, service = BundleService.class)
public class BundleService {
  @Reference
  private ResourceSearchService searchService;
  @Reference
  private BundleReferenceHandler bundleReferenceHandler;
  @Reference
  private BundleEntryCxfProcessor bundleEntryProcessor;
  @Reference
  private TransactionService tx;

  public Bundle save(Bundle bundle, String prefer) {
    if (bundle.getEntry().stream().anyMatch(e -> !e.hasRequest())) {
      throw new FhirException(400, IssueType.INVALID, "Bundle.request element required");
    }

    if (bundle.getType() == BundleType.BATCH) {
      bundle.getEntry().sort(new EntityMethodOrderComparator());
      return batch(bundle, prefer);
    }
    if (bundle.getType() == BundleType.TRANSACTION) {
      validateTransaction(bundle);
      bundleReferenceHandler.replaceIds(bundle);
      bundle.getEntry().sort(new EntityMethodOrderComparator()); //moved after replaceIds because incorrect behavior in case of cinditional updates
      return transaction(bundle, prefer);
    }
    throw new FhirException(400, IssueType.INVALID, "only batch or transaction supported");
  }

  private void validateTransaction(Bundle bundle) {
    bundle.getEntry().forEach(entry -> {
      if (entry.getRequest().getMethod() == HTTPVerb.POST && entry.getRequest().getIfNoneExist() != null) {
        String ifNoneExist = entry.getRequest().getIfNoneExist() + "&_count=0";
        String type = entry.getResource().getResourceType().name();
        SearchCriterion criteria = new SearchCriterion(type, SearchUtil.parse(ifNoneExist, type));
        SearchResult result = searchService.search(criteria);
        if (result.getTotal() == 1) {
          entry.getRequest().setMethod(HTTPVerb.NULL); //ignore
        }
        if (result.getTotal() > 1) {
          String msg = "was expecting 0 or 1 resources. found " + result.getTotal();
          throw new FhirException(412, IssueType.PROCESSING, msg);
        }
      }
    });
  }

  private Bundle batch(Bundle bundle, String prefer) {
    Bundle responseBundle = new Bundle();
    bundle.getEntry().forEach(entry -> {
      try {
        responseBundle.addEntry(perform(entry, prefer));
      } catch (Exception e) {
        FhirException fhirException = findFhirException(e);
        if (fhirException != null) {
          BundleEntryResponseComponent responseEntry = new BundleEntryResponseComponent();
          responseEntry.setStatus("" + fhirException.getStatusCode());
          OperationOutcome outcome = new OperationOutcome();
          outcome.setIssue(fhirException.getIssues());
          responseEntry.setOutcome(outcome);
          BundleEntryComponent responseBundleEntry = responseBundle.addEntry();
          responseBundleEntry.addLink().setRelation("alternate").setUrl(entry.getFullUrl());
          responseBundleEntry.setResponse(responseEntry);
          return;
        }
        throw new RuntimeException("entry: " + entry.getFullUrl(), e.getCause());
      }
    });
    responseBundle.setType(BundleType.BATCHRESPONSE);
    return responseBundle;
  }

  private Bundle transaction(Bundle bundle, String prefer) {
    return tx.transaction(() -> {
      Bundle responseBundle = new Bundle();
      bundle.getEntry().forEach(entry -> {
        try {
          responseBundle.addEntry(perform(entry, prefer));
        } catch (Exception e) {
          FhirException fhirException = findFhirException(e);
          if (fhirException != null) {
            fhirException.addExtension("fullUrl", entry.getFullUrl());
            fhirException.getIssues().forEach(i -> {
              String expr = "Bundle.entry[" + bundle.getEntry().indexOf(entry) + "]";
              i.addExpression(expr);
            });
          }
          throw e;
        }
      });
      responseBundle.setType(BundleType.TRANSACTIONRESPONSE);
      return responseBundle;
    });
  }

  private BundleEntryComponent perform(BundleEntryComponent entry, String prefer) {
    if (entry.getRequest().getMethod() == HTTPVerb.NULL) {
      //XXX hack  @see #validateTransaction
      return new BundleEntryComponent().setResponse(new BundleEntryResponseComponent().setStatus("200"));
    }
    BundleEntryComponent responseEntry = bundleEntryProcessor.perform(entry, prefer);
    responseEntry.addLink().setRelation("alternate").setUrl(entry.getFullUrl());
    return responseEntry;
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
