package io.github.jon1van.func;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.function.Consumer;

/// This class provides Collections that implement Consumer.
///
/// These classes can help debug data processing pipelines that use Consumer but still need to "collect and inspect"
/// the passing data.
public class ConsumingCollections {

    private ConsumingCollections() {
        throw new IllegalStateException("Do Not Instatiate");
    }

    ///  An ArrayList that implements Consumer
    public static class ConsumingArrayList<T> extends ArrayList<T> implements Consumer<T> {

        @Override
        public void accept(T t) {
            add(t);
        }
    }

    public static <E> ConsumingArrayList<E> newConsumingArrayList() {
        return new ConsumingArrayList<>();
    }

    ///  A LinkedList that implements Consumer
    public static class ConsumingLinkedList<T> extends LinkedList<T> implements Consumer<T> {

        @Override
        public void accept(T t) {
            add(t);
        }
    }

    public static <E> ConsumingLinkedList<E> newConsumingLinkedList() {
        return new ConsumingLinkedList<>();
    }

    ///  A HashSet that implements Consumer
    public static class ConsumingHashSet<T> extends HashSet<T> implements Consumer<T> {

        @Override
        public void accept(T t) {
            add(t);
        }
    }

    public static <E> ConsumingHashSet<E> newConsumingHashSet() {
        return new ConsumingHashSet<>();
    }

    ///  A TreeSet that implements Consumer
    public static class ConsumingTreeSet<T> extends TreeSet<T> implements Consumer<T> {

        @Override
        public void accept(T t) {
            add(t);
        }
    }

    public static <E> ConsumingTreeSet<E> newConsumingTreeSet() {
        return new ConsumingTreeSet<>();
    }

    ///  A PriorityQueue that implements Consumer
    public static class ConsumingPriorityQueue<T> extends PriorityQueue<T> implements Consumer<T> {

        @Override
        public void accept(T t) {
            add(t);
        }
    }

    public static <E> ConsumingPriorityQueue<E> newConsumingPriorityQueue() {
        return new ConsumingPriorityQueue<>();
    }
}
