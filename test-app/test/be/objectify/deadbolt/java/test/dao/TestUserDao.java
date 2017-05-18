/*
 * Copyright 2010-2017 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.objectify.deadbolt.java.test.dao;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import be.objectify.deadbolt.java.test.models.SecurityPermission;
import be.objectify.deadbolt.java.test.models.SecurityRole;
import be.objectify.deadbolt.java.test.models.User;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class TestUserDao implements UserDao {

    private final Map<String, User> users = new HashMap<>();

    public TestUserDao() {
        final SecurityRole fooRole = new SecurityRole("foo");
        final SecurityRole barRole = new SecurityRole("bar");
        final SecurityRole hurdyRole = new SecurityRole("hurdy");

        final SecurityPermission kuzPermission = new SecurityPermission("killer.undead.zombie");
        final SecurityPermission kuvPermission = new SecurityPermission("killer.undead.vampire");
        final SecurityPermission cmiPermission = new SecurityPermission("curator.museum.insects");
        final SecurityPermission zmePermission = new SecurityPermission("zombie.movie.enthusiast");

        users.put("greet",
                  new User("greet",
                           Arrays.asList(fooRole, barRole),
                           Collections.singletonList(kuzPermission)));
        users.put("lotte",
                  new User("lotte",
                           Collections.singletonList(hurdyRole),
                           Collections.singletonList(kuvPermission)));
        users.put("steve",
                  new User("steve",
                           Collections.singletonList(barRole),
                           Collections.singletonList(cmiPermission)));
        users.put("mani",
                  new User("mani",
                           Arrays.asList(fooRole, barRole, hurdyRole),
                           Collections.singletonList(zmePermission)));
        users.put("trippel",
                  new User("trippel",
                           Arrays.asList(fooRole, hurdyRole),
                           Collections.emptyList()));
    }

    @Override
    public Optional<User> getByUserName(final String userName) {
        return Optional.ofNullable(users.get(userName));
    }
}
