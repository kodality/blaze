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
 package com.kodality.fhir.rest.util;

import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.HttpHeaders;

import java.util.stream.Stream;

public final class PreferredReturn {
  private static final String type = "return";
  public static final String minimal = "minimal";
  public static final String representation = "representation";
  public static final String OperationOutcome = "OperationOutcome";

  private PreferredReturn() {
    //
  }

  public static final String parse(HttpHeaders headers) {
    if(headers == null) {
      return null;
    }
    return parse(headers.getHeaderString("Prefer"));
  }

  public static final String parse(String preferredHeader) {
    if (preferredHeader == null) {
      return null;
    }
    return Stream.of(StringUtils.split(preferredHeader, ";"))
        .map(p -> StringUtils.split(p, "="))
        .filter(p -> p[0].equals(type))
        .findAny()
        .map(p -> p[1])
        .orElse(null);
  }

}
