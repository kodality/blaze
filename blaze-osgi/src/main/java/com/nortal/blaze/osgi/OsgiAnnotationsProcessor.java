package com.nortal.blaze.osgi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.felix.scrplugin.SCRDescriptorException;
import org.apache.felix.scrplugin.SCRDescriptorFailureException;
import org.apache.felix.scrplugin.SpecVersion;
import org.apache.felix.scrplugin.annotations.AnnotationProcessor;
import org.apache.felix.scrplugin.annotations.ClassAnnotation;
import org.apache.felix.scrplugin.annotations.FieldAnnotation;
import org.apache.felix.scrplugin.annotations.MethodAnnotation;
import org.apache.felix.scrplugin.annotations.ScannedAnnotation;
import org.apache.felix.scrplugin.annotations.ScannedClass;
import org.apache.felix.scrplugin.description.ClassDescription;
import org.apache.felix.scrplugin.description.ComponentConfigurationPolicy;
import org.apache.felix.scrplugin.description.ComponentDescription;
import org.apache.felix.scrplugin.description.PropertyDescription;
import org.apache.felix.scrplugin.description.PropertyType;
import org.apache.felix.scrplugin.description.PropertyUnbounded;
import org.apache.felix.scrplugin.description.ReferenceCardinality;
import org.apache.felix.scrplugin.description.ReferenceDescription;
import org.apache.felix.scrplugin.description.ReferencePolicy;
import org.apache.felix.scrplugin.description.ReferencePolicyOption;
import org.apache.felix.scrplugin.description.ReferenceStrategy;
import org.apache.felix.scrplugin.description.ServiceDescription;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

public class OsgiAnnotationsProcessor implements AnnotationProcessor {

  /**
   * @see org.apache.felix.scrplugin.annotations.AnnotationProcessor#getName()
   */
  @Override
  public String getName() {
    return "DS Annotation Processor";
  }

  /**
   * @see org.apache.felix.scrplugin.annotations.AnnotationProcessor#process(org.apache.felix.scrplugin.annotations.ScannedClass,
   *      org.apache.felix.scrplugin.description.ClassDescription)
   */
  @Override
  public void process(final ScannedClass scannedClass, final ClassDescription describedClass)
      throws SCRDescriptorFailureException, SCRDescriptorException {
    final List<ClassAnnotation> componentTags = scannedClass.getClassAnnotations(Component.class.getName());
    scannedClass.processed(componentTags);

    for (final ClassAnnotation cad : componentTags) {
      this.createComponent(cad, describedClass, scannedClass);
    }

    // search for the component descriptions and use the first one
    final List<ComponentDescription> componentDescs = describedClass.getDescriptions(ComponentDescription.class);
    ComponentDescription found = null;
    if (!componentDescs.isEmpty()) {
      found = componentDescs.get(0);
    }

    if (found != null) {
      final ComponentDescription cd = found;

      // search for methods
      final List<MethodAnnotation> methodTags = scannedClass.getMethodAnnotations(null);
      for (final MethodAnnotation m : methodTags) {
        if (m.getName().equals(Activate.class.getName())) {
          cd.setActivate(m.getAnnotatedMethod().getName());
          scannedClass.processed(m);
        } else if (m.getName().equals(Deactivate.class.getName())) {
          cd.setDeactivate(m.getAnnotatedMethod().getName());
          scannedClass.processed(m);
        } else if (m.getName().equals(Modified.class.getName())) {
          cd.setModified(m.getAnnotatedMethod().getName());
          scannedClass.processed(m);
        } else if (m.getName().equals(Reference.class.getName())) {
          this.processReference(describedClass, m);
          scannedClass.processed(m);
        }
      }
      scannedClass.getFieldAnnotations(Reference.class.getName()).forEach(fa -> {
        this.processReference(describedClass, fa);
      });

    }
  }

  /**
   * @see org.apache.felix.scrplugin.annotations.AnnotationProcessor#getRanking()
   */
  @Override
  public int getRanking() {
    return 300;
  }

  /**
   * Create a component description.
   *
   * @param cad The component annotation for the class.
   * @param scannedClass The scanned class.
   */
  private ComponentDescription createComponent(final ClassAnnotation cad,
                                               final ClassDescription describedClass,
                                               final ScannedClass scannedClass)
      throws SCRDescriptorException {
    final ComponentDescription component = new ComponentDescription(cad);
    describedClass.add(component);

    // Although not defined in the spec, we support abstract classes.
    final boolean classIsAbstract = Modifier.isAbstract(scannedClass.getClass().getModifiers());
    component.setAbstract(classIsAbstract);

    // name
    component.setName(cad.getStringValue("name", scannedClass.getScannedClass().getName()));

    // services
    final List<String> listedInterfaces = new ArrayList<String>();
    if (cad.hasValue("service")) {
      final String[] interfaces = (String[]) cad.getValue("service");
      if (interfaces != null) {
        for (final String t : interfaces) {
          listedInterfaces.add(t);
        }
      }
    } else {
      // scan directly implemented interfaces
      this.searchInterfaces(listedInterfaces, scannedClass.getScannedClass());
    }
    if (listedInterfaces.size() > 0) {
      final ServiceDescription serviceDesc = new ServiceDescription(cad);
      describedClass.add(serviceDesc);

      for (final String name : listedInterfaces) {
        serviceDesc.addInterface(name);
      }
      serviceDesc.setServiceFactory(cad.getBooleanValue("servicefactory", false));
    }

    // factory
    component.setFactory(cad.getStringValue("factory", null));

    // enabled
    if (cad.getValue("enabled") != null) {
      component.setEnabled(cad.getBooleanValue("enabled", true));
    }

    // immediate
    if (cad.getValue("immediate") != null) {
      component.setImmediate(cad.getBooleanValue("immediate", false));
    }

    // property
    final String[] property = (String[]) cad.getValue("property");
    if (property != null) {
      // TODO - what do we do if the value is invalid?
      for (final String propDef : property) {
        final int pos = propDef.indexOf('=');
        if (pos != -1) {
          final String prefix = propDef.substring(0, pos);
          final String value = propDef.substring(pos + 1);
          final int typeSep = prefix.indexOf(':');
          final String key = (typeSep == -1 ? prefix : prefix.substring(0, typeSep));
          final String type = (typeSep == -1 ? PropertyType.String.name() : prefix.substring(typeSep + 1));

          final PropertyType propType = PropertyType.valueOf(type);
          // FELIX-4159 : check if this is a multi value prop
          final List<PropertyDescription> existingProps = describedClass.getDescriptions(PropertyDescription.class);
          PropertyDescription found = null;
          for (final PropertyDescription current : existingProps) {
            if (current.getName().equals(key)) {
              found = current;
              break;
            }
          }
          if (found == null) {
            final PropertyDescription pd = new PropertyDescription(cad);
            describedClass.add(pd);
            pd.setName(key);
            pd.setValue(value);
            pd.setType(propType);
            pd.setUnbounded(PropertyUnbounded.DEFAULT);
          } else {
            if (propType != found.getType()) {
              throw new SCRDescriptorException("Multi value property '" + key + "' has different types: "
                  + found.getType() + " & " + propType, describedClass.getSource());
            }
            if (found.getValue() != null) {
              final String[] values = new String[2];
              values[0] = found.getValue();
              values[1] = value;
              found.setMultiValue(values);
            } else {
              final String[] oldValues = found.getMultiValue();
              final String[] newValues = new String[oldValues.length + 1];
              System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
              newValues[oldValues.length] = value;
              found.setMultiValue(newValues);
            }
          }
        }
      }
    }
    // TODO: properties

    // xmlns
    if (cad.getValue("xmlns") != null) {
      final SpecVersion spec = SpecVersion.fromNamespaceUrl(cad.getValue("xmlns").toString());
      if (spec == null) {
        throw new SCRDescriptorException("Unknown xmlns attribute value: " + cad.getValue("xmlns"),
                                         describedClass.getSource());
      }
      component.setSpecVersion(spec);
    }

    // configuration policy
    component.setConfigurationPolicy(ComponentConfigurationPolicy.valueOf(cad.getEnumValue("configurationPolicy",
                                                                                           ComponentConfigurationPolicy.OPTIONAL.name())));

    // configuration pid
    Object configPid = cad.getValue("configurationPid");
    if (configPid instanceof String) {
      component.setConfigurationPid((String) configPid);
    } else if (configPid instanceof String[] && ((String[]) configPid).length == 1) {
      component.setConfigurationPid(((String[]) configPid)[0]);
    } else {
      component.setConfigurationPid(null);
    }
    // component.setCreatePid(false);

    // no inheritance
    component.setInherit(false);

    return component;
  }

  /**
   * Get all directly implemented interfaces
   */
  private void searchInterfaces(final List<String> interfaceList, final Class<?> javaClass) {
    final Class<?>[] interfaces = javaClass.getInterfaces();
    for (final Class<?> i : interfaces) {
      interfaceList.add(i.getName());
    }
  }

  /**
   * Process a reference
   */
  private void processReference(final ClassDescription describedClass, final ScannedAnnotation sa) {
    final ReferenceDescription ref = new ReferenceDescription(sa);
    describedClass.add(ref);

    String defaultName = null;
    String defaultService = null;
    String defaultBind = null;
    String defaultUnbind = null;
    String defaultUpdated = null;
    String defaultCardinality = null;
    String defaultPolicy = null;

    if (sa instanceof FieldAnnotation) {
      FieldAnnotation fa = (FieldAnnotation) sa;
      Field field = fa.getAnnotatedField();
      Class<?> fieldType = field.getType();
      String fieldName = field.getName();
      ref.setField(field);

      defaultPolicy = Modifier.isVolatile(field.getModifiers())
                                                                ? ReferencePolicy.DYNAMIC.name()
                                                                : ReferencePolicy.STATIC.name();
      defaultName = fieldName;
      if (fieldType.isArray()) {
        defaultService = fieldType.getComponentType().getName();
        defaultCardinality = org.osgi.service.component.annotations.ReferenceCardinality.MULTIPLE.name();
      } else if (Collection.class.isAssignableFrom(fieldType)) {
        defaultService = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0].getTypeName();
        defaultCardinality = org.osgi.service.component.annotations.ReferenceCardinality.MULTIPLE.name();
      } else {
        defaultService = fieldType.getName();
        defaultCardinality = org.osgi.service.component.annotations.ReferenceCardinality.MANDATORY.name();
      }
    }

    if (sa instanceof MethodAnnotation) {
      MethodAnnotation ma = (MethodAnnotation) sa;
      defaultCardinality = org.osgi.service.component.annotations.ReferenceCardinality.MANDATORY.name();
      defaultPolicy = ReferencePolicy.STATIC.name();

      defaultBind = ma.getAnnotatedMethod().getName();
      final String methodName = ma.getAnnotatedMethod().getName();
      if (methodName.startsWith("add")) {
        defaultName = methodName.substring(3);
        defaultUnbind = "remove" + defaultName;
      } else if (methodName.startsWith("set")) {
        defaultName = methodName.substring(3);
        defaultUnbind = "unset" + defaultName;
      } else if (methodName.startsWith("bind")) {
        defaultName = methodName.substring(4);
        defaultUnbind = "unbind" + defaultName;
      } else {
        defaultName = methodName;
        defaultUnbind = "un" + defaultName;
      }
      defaultUpdated = "updated" + defaultName;
      final Class<?>[] params = ma.getAnnotatedMethod().getParameterTypes();
      defaultService = params != null && params.length > 0 ? params[0].getName() : null;
    }

    ref.setStrategy(ReferenceStrategy.EVENT);
    ref.setName(sa.getStringValue("name", defaultName));
    ref.setInterfaceName(sa.getStringValue("service", defaultService));
    ref.setBind(sa.getStringValue("bind", hasMethod(describedClass, defaultBind) ? defaultBind : null));
    ref.setBind(sa.getStringValue("unbind", hasMethod(describedClass, defaultUnbind) ? defaultUnbind : null));
    ref.setBind(sa.getStringValue("updated", hasMethod(describedClass, defaultUpdated) ? defaultUpdated : null));

    ref.setPolicy(ReferencePolicy.valueOf(sa.getEnumValue("policy", defaultPolicy)));
    ref.setTarget(sa.getStringValue("target", null));
    ref.setPolicyOption(ReferencePolicyOption.valueOf(sa.getEnumValue("policyOption",
                                                                      ReferencePolicyOption.RELUCTANT.name())));
    final String cardinality = sa.getEnumValue("cardinality", defaultCardinality);
    if (cardinality.equals("OPTIONAL")) {
      ref.setCardinality(ReferenceCardinality.OPTIONAL_UNARY);
    } else if (cardinality.equals("MULTIPLE")) {
      ref.setCardinality(ReferenceCardinality.OPTIONAL_MULTIPLE);
    } else if (cardinality.equals("AT_LEAST_ONE")) {
      ref.setCardinality(ReferenceCardinality.MANDATORY_MULTIPLE);
    } else {
      ref.setCardinality(ReferenceCardinality.MANDATORY_UNARY);
    }
  }

  private boolean hasMethod(final ClassDescription classDescription, final String name) {
    final Method[] allMethods = classDescription.getDescribedClass().getDeclaredMethods();
    for (final Method m : allMethods) {
      if (m.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

}
