package com.nortal.blaze.fhir.structure.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FhirXpath {
  private static final String UTF_8 = "UTF-8";
  private static final XPathFactory FACTORY =  XPathFactory.newInstance();

  private static XPath newInstance() {
    XPath xpath = FACTORY.newXPath();
    xpath.setNamespaceContext(new FhirNamespaceContext());
    return xpath;
  }

  public static Node parse(String xml) {
    try {
      InputStream is = IOUtils.toInputStream(xml, UTF_8);
      return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
    } catch (SAXException | IOException | ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  public static NodeList eval(String xml, String xpath) {
    return eval(parse(xml), xpath);
  }

  public static NodeList eval(Node xml, String xpath) {
    try {
      XPathExpression expr = newInstance().compile(xpath);
      return (NodeList) expr.evaluate(xml, XPathConstants.NODESET);
    } catch (XPathException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<String> text(String xml, String xpath) {
    NodeList nodes = eval(xml, xpath);
    List<String> result = new ArrayList<>();
    if (nodes.getLength() == 0) {
      return result;
    }
    for (int i = 0; i < nodes.getLength(); i++) {
      result.add(nodes.item(i).getTextContent());
    }
    return result;
  }

  public static class FhirNamespaceContext implements NamespaceContext {
    private static final String URI = "http://hl7.org/fhir";
    private static final String PREFIX = "f";

    @Override
    public String getNamespaceURI(String prefix) {
      return URI;
    }

    @Override
    public String getPrefix(String namespaceURI) {
      return PREFIX;
    }

    @Override
    public Iterator<?> getPrefixes(String namespaceURI) {
      return Collections.singleton(getPrefix(namespaceURI)).iterator();
    }
  }
}
