package org.mitre.caasd.commons.out;

import static java.util.Objects.requireNonNull;

import java.util.Collection;

public class Sinks {

    /** This OutputSink does nothing. It is for testing when the OutputSink is irrelevant */
    public static <T> OutputSink<T> noOpSink() {
        return new NoOpSink<>();
    }

    public static class NoOpSink<T> implements OutputSink<T> {

        @Override
        public void accept(T record) {
            // do nothing
        }
    }

    /**
     * This OutputSink adds all input to the collection.
     *
     * <p>It is for testing when the data sent to the OutputSink is under test.  This OutputSink is
     * also useful when the dataset being collected is small enough to fit in memory.
     */
    public static <T> CollectionSink<T> collectionSink(Collection<T> addToMe) {
        return new CollectionSink<>(addToMe);
    }

    public static class CollectionSink<T> implements OutputSink<T> {

        private final Collection<T> col;

        public CollectionSink(Collection<T> col) {
            this.col = requireNonNull(col);
        }

        @Override
        public void accept(T record) {
            col.add(record);
        }

        public Collection<T> collection() {
            return this.col;
        }
    }
}
