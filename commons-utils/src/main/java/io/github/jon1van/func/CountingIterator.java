package io.github.jon1van.func;

import java.util.Iterator;

/// A CountingIterator decorates the provided Iterator with a counting mechanic that is triggered on
/// every call to next.
///
/// A CountingIterator is designed to provide stats on how many items streamed through a Iterator
/// in its lifetime (assuming the root Iterator was wrapped throughout its lifetime)
public class CountingIterator<T> implements Iterator<T> {

    int numCallsToNext = 0;

    private final Iterator<T> innerIter;

    public CountingIterator(Iterator<T> iter) {
        this.innerIter = iter;
    }

    public int numCallsNext() {
        return numCallsToNext;
    }

    @Override
    public boolean hasNext() {
        return innerIter.hasNext();
    }

    @Override
    public T next() {
        numCallsToNext++;
        return innerIter.next();
    }

    public Iterator<T> innerIterator() {
        return innerIter;
    }
}
