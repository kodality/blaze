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
 package com.kodality.blaze.auth.http.yupi;

import com.kodality.blaze.auth.User;
import com.kodality.blaze.auth.http.AuthHeaderAuthenticator;
import com.kodality.blaze.auth.http.HttpAuthorization;
import org.apache.cxf.message.Message;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(immediate = true, service = AuthHeaderAuthenticator.class)
public class YupiAuthenticator implements AuthHeaderAuthenticator {
  private static final Map<String, User> yupis = new HashMap<>();
  static {
    yupis.put("yupi", makeYupi("yupi", "yupland"));
    yupis.put("ipuy", makeYupi("ipuy", "dnalpuy"));
  }

  @Override
  public User autheticate(List<HttpAuthorization> auths, Message message) {
    return auths.stream().filter(a -> a.isType("Bearer")).map(bearer -> {
      return yupis.containsKey(bearer.getCredential()) ? yupis.get(bearer.getCredential()) : null;
    }).filter(u -> u != null).findFirst().orElse(null);
  }

  private static User makeYupi(String sub, String org) {
    User user = new User();
    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", sub);
    claims.put("org", org);
    user.setClaims(claims);
    user.setScopes(Collections.singleton("user/*.*"));
    return user;
  }

}
