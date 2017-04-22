/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.morimekta.testing.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Collection;

/**
 * All items in a collection matches the given matcher.
 *
 * This is equivalent with a loop over all items in the collection
 * and calling <code>assertThat(item, matcher)</code>
 */
public class AllItemsMatch<T> extends BaseMatcher<Collection<T>> {
    private final Matcher<T> matcher;

    public AllItemsMatch(Matcher<T> matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean matches(Object item) {
        if (!(item instanceof Collection)) {
            return false;
        }
        for (Object t : ((Collection) item)) {
            if (!matcher.matches(t)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("all items match: ");
        matcher.describeTo(description);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void describeMismatch(Object item, Description description) {
        if (!(item instanceof Collection)) {
            description.appendText("not a collection: ")
                       .appendValue(item);
            return;
        }
        description.appendValueList("was [", ", ", "]", ((Collection<Object>) item));
    }
}
