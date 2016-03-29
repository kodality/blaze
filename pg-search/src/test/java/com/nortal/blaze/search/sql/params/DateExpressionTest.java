package com.nortal.blaze.search.sql.params;

import com.nortal.blaze.core.model.search.QueryParam;
import com.nortal.blaze.util.sql.SqlBuilder;
import com.nortal.fhir.conformance.operations.TestSearchParameterMonitor;
import java.util.Arrays;
import org.hl7.fhir.instance.model.Enumerations.SearchParamType;
import org.hl7.fhir.instance.model.SearchParameter;
import org.junit.Assert;
import org.junit.Before;

public class DateExpressionTest {
  private final DateExpressionProvider testMe = new DateExpressionProvider();

  @Before
  public void mocks() {
    SearchParameter sp = new SearchParameter();
    sp.setBase("NotAResource");
    sp.setCode("barabashka");
    sp.setXpath("h.u.y");
    TestSearchParameterMonitor.apply(sp);
  }

//  @Test
  public void test() {
    test(null, null, null, null);
    test("", null, null, null);
    test("1111", "&&", "1111-01-01T00:00:00", "1 year");
    test("le1111", "<", "1111-01-01T00:00:00", "1 year");
    test("lt1111", "<<", "1111-01-01T00:00:00", "1 year");
    test("ge1111", ">", "1111-01-01T00:00:00", "1 year");
    test("gt1111", ">>", "1111-01-01T00:00:00", "1 year");
    test("1111-11", "&&", "1111-11-01T00:00:00", "1 month");
    test("1111-11-11", "&&", "1111-11-11T00:00:00", "1 day");
    test("1111-11-11T11", "&&", "1111-11-11T11:00:00", "1 hour");
    test("1111-11-11T11:11", "&&", "1111-11-11T11:11:00", "1 minute");
    test("1111-11-11T11:11:11", "&&", "1111-11-11T11:11:11", "1 second");
  }

  private void test(String input, String op, String date, String range) {
    QueryParam param = new QueryParam("barabashka", null, SearchParamType.DATE, "NotAResource");
    param.setValues(Arrays.asList(input));
    SqlBuilder result = testMe.makeExpression(param, "a");
    if (date == null && range == null) {
      Assert.assertEquals("", result.getSql());
    } else {
      Assert.assertEquals("date(a, 'h.u.y') " + op + " range('" + date + "', '" + range + "')", result.getSql());
    }
  }
}
