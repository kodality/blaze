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
 package com.kodality.blaze.search.sql.params;

import com.kodality.blaze.core.model.search.QueryParam;
import com.kodality.blaze.search.dao.BlindexDao;
import com.kodality.blaze.search.model.Blindex;
import com.kodality.blaze.service.conformance.TestConformanceHolder;
import com.kodality.blaze.util.sql.SqlBuilder;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.SearchParameter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class DateExpressionTest {
  private final DateExpressionProvider testMe = new DateExpressionProvider();

  @Before
  public void mocks() {
    SearchParameter sp = new SearchParameter();
    sp.setBase(Collections.singletonList(new CodeType("NotAResource")));
    sp.setCode("barabashka");
    sp.setExpression("NotAResource.h.o.y");
    TestConformanceHolder.apply(sp);

    new BlindexDao() {
      @Override
      public java.util.List<Blindex> load(String type) {
        Blindex b = new Blindex("NotAResource", "h.o.y");
        b.setName("parasol");
        return Collections.singletonList(b);
      };
    }.init();
  }

  @Test
  public void test() {
    test(null, null);
    test("", null);
    test("1111", "range && range('1111-01-01T00:00:00+00:00', '1 year')");
    test("le1111", "(range && range('1111-01-01T00:00:00+00:00', '1 year') OR range << range('1111-01-01T00:00:00+00:00', '1 year'))");
    test("lt1111", "range << range('1111-01-01T00:00:00+00:00', '1 year')");
    test("ge1111", "(range && range('1111-01-01T00:00:00+00:00', '1 year') OR range >> range('1111-01-01T00:00:00+00:00', '1 year'))");
    test("gt1111", "range >> range('1111-01-01T00:00:00+00:00', '1 year')");
    test("1111-11", "range && range('1111-11-01T00:00:00+00:00', '1 month')");
    test("1111-11-11", "range && range('1111-11-11T00:00:00+00:00', '1 day')");
    test("1111-11-11T11", "range && range('1111-11-11T11:00:00+00:00', '1 hour')");
    test("1111-11-11T11:11", "range && range('1111-11-11T11:11:00+00:00', '1 minute')");
    test("1111-11-11T11:11:11", "range && range('1111-11-11T11:11:11+00:00', '1 second')");

    test("1111-11-11T11:11:11Z", "range && range('1111-11-11T11:11:11+00:00', '1 second')");
    test("1111-11-11T11:11:11+07:00", "range && range('1111-11-11T11:11:11+07:00', '1 second')");
  }

  private void test(String input, String expectedCondition) {
    QueryParam param = new QueryParam("barabashka", null, SearchParamType.DATE, "NotAResource");
    param.setValues(Arrays.asList(input));
    SqlBuilder result = testMe.makeExpression(param, "a");
    if (expectedCondition == null) {
      Assert.assertEquals("", result.getSql());
    } else {
      String expected =
          String.format("EXISTS (SELECT 1 FROM parasol WHERE resource_key = a.key  AND %s)", expectedCondition);
      Assert.assertEquals(expected, result.getSql());
    }
  }
}
