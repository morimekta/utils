package net.morimekta.testing.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Matcher to check that two sets does not have any common elements.
 *
 * The elements must have proper equals method implementations for this matcher to work.
 */
public class DistinctFrom<T> extends BaseMatcher<Set<T>> {
    private final Set<T> from;

    public DistinctFrom(Set<T> from) {
        this.from = from;
    }

    @Override
    public boolean matches(Object o) {
        if (o == null || !(o instanceof Set))
            return false;
        @SuppressWarnings("unchecked")
        Set<String> other = (Set<String>) o;
        return other.stream()
                    .noneMatch(from::contains);
    }

    @Override
    public void describeMismatch(Object o, Description description) {
        if (o == null || !(o instanceof Collection)) {
            return;
        }

        @SuppressWarnings("unchecked")
        Collection<T> other = (Collection<T>) o;
        Set<T> common = other.stream()
                             .filter(from::contains)
                             .collect(Collectors.toSet());

        description.appendValueList("both contained [", ", ", "]", common);
    }

    @Override
    public void describeTo(Description description) {
        description.appendValueList("distinct from [", ", ", "]", from);
    }
}
