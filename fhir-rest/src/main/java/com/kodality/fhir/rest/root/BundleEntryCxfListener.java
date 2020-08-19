package com.kodality.fhir.rest.root;

import java.lang.reflect.Method;
import java.net.URI;

public interface BundleEntryCxfListener {
  void beforeInvoke(Method meth, URI uri);
}
