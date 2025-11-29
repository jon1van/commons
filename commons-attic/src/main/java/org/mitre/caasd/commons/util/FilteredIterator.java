package org.mitre.caasd.commons.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * A FilteredIterator combines an Iterator and a Predicate to create a new Iterator that only
 * provides items that (A) came from unprotected Iterator and (B) satisfy the Predicate's test.
 * <p>
 * Using a FilteredIterator makes certain code easier to read and write because you no longer have
 * to manually check against a predicate.
 * <p>
 * //@todo -- Move this class to Commons
 */
public class FilteredIterator<T> implements Iterator<T> {

    private final Iterator<T> innerIterator;

    private final Predicate<T> predicate;

    private T next;

    public FilteredIterator(Iterator<T> iter, Predicate<T> predicate) {
        checkNotNull(iter);
        checkNotNull(predicate);
        this.innerIterator = iter;
        this.predicate = predicate;
        this.next = findNext();
    }

    public static <E> Iterator<E> filter(Iterator<E> iter, Predicate<E> predicate) {
        return new FilteredIterator(iter, predicate);
    }

    private T findNext() {

        while (innerIterator.hasNext()) {
            T candidate = innerIterator.next();
            if (predicate.test(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public T next() {
        if (next == null) {
            throw new NoSuchElementException();
        }

        T returnMe = next;
        next = findNext();
        return returnMe;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported");
    }
}
