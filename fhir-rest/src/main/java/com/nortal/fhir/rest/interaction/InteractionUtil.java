package com.nortal.fhir.rest.interaction;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.model.UserOperation;
import org.apache.cxf.jaxrs.utils.AnnotationUtils;
import org.apache.cxf.jaxrs.utils.ResourceUtils;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.ResourceInteractionComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.SystemInteractionComponent;

public final class InteractionUtil {
  private InteractionUtil() {
    //
  }

  public static List<UserOperation> getOperations(CapabilityStatementRestComponent capability, Class<?> server) {
    List<UserOperation> ops = new ArrayList<>();
    for (SystemInteractionComponent resourceInteraction : capability.getInteraction()) {
      if (resourceInteraction.getCode() == null) {
        continue;
      }
      ops.addAll(create(resourceInteraction.getCode().toCode(), server));
    }
    return ops;
  }

  public static List<UserOperation> getOperations(CapabilityStatementRestResourceComponent capability,
                                                  Class<?> server) {
    List<UserOperation> ops = new ArrayList<>();
    for (ResourceInteractionComponent resourceInteraction : capability.getInteraction()) {
      if (resourceInteraction.getCode() == null) {
        continue;
      }
      ops.addAll(create(resourceInteraction.getCode().toCode(), server));
    }
    return ops;
  }

  public static List<UserOperation> create(String interaction, Class<?> server) {
    List<Method> methods = findMethods(interaction, server);
    if (CollectionUtils.isEmpty(methods)) {
      return Collections.emptyList();
    }
    List<UserOperation> result = new ArrayList<>();
    for (Method m : methods) {
      Path path = m.getAnnotation(Path.class);
      UserOperation op = new UserOperation(m.getName(), path == null ? "" : path.value());
      op.setVerb(AnnotationUtils.getHttpMethodValue(m));
      op.setParameters(ResourceUtils.getParameters(m));
      setConsumes(op, m);
      result.add(op);
    }
    return result;
  }

  private static void setConsumes(UserOperation op, Method m) {
    Consumes cm = AnnotationUtils.getMethodAnnotation(m, Consumes.class);
    if (cm == null) {
      return;
    }
    op.setConsumes(StringUtils.join(cm.value(), ","));
  }

  private static List<Method> findMethods(String interaction, Class<?> clazz) {
    if (interaction == null) {
      return Collections.emptyList();
    }
    try {
      List<Method> result = new ArrayList<>();
      for (Method m : clazz.getMethods()) {
        if (interaction.equals(getMethodInteraction(m))) {
          result.add(m);
        }
      }
      return result;
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    }
  }

  private static String getMethodInteraction(Method m) {
    Interaction interaction = m.getAnnotation(Interaction.class);
    if (interaction == null) {
      return null;
    }
    return interaction.value();
  }

}
