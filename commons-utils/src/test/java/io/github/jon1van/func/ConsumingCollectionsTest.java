package io.github.jon1van.func;

import static io.github.jon1van.func.ConsumingCollections.*;
import static org.assertj.core.api.Assertions.*;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.TreeSet;

import io.github.jon1van.func.ConsumingCollections.ConsumingArrayList;
import io.github.jon1van.func.ConsumingCollections.ConsumingHashSet;
import org.junit.jupiter.api.Test;

class ConsumingCollectionsTest {

    @Test
    public void testConsumingArrayList() {

        ConsumingArrayList<Integer> consumer = newConsumingArrayList();

        assertThat(consumer.isEmpty()).isTrue();

        consumer.accept(5);

        assertThat(consumer.size()).isEqualTo(1);
        assertThat(consumer.get(0)).isEqualTo(5);

        consumer.accept(12);

        assertThat(consumer.size()).isEqualTo(2);
        assertThat(consumer.get(0)).isEqualTo(5);
        assertThat(consumer.get(1)).isEqualTo(12);

        consumer.clear();

        assertThat(consumer.isEmpty()).isTrue();
    }

    @Test
    public void testConsumingHashSet() {
        ConsumingHashSet<Integer> consumer = newConsumingHashSet();

        assertThat(consumer.isEmpty()).isTrue();

        consumer.accept(5);

        assertThat(consumer.size()).isEqualTo(1);
        assertThat(consumer.contains(5)).isTrue();

        consumer.accept(12);

        assertThat(consumer.size()).isEqualTo(2);
        assertThat(consumer.contains(5)).isTrue();
        assertThat(consumer.contains(12)).isTrue();

        consumer.clear();

        assertThat(consumer.isEmpty()).isTrue();
    }

    @Test
    public void linkedListAggregatorWorks() {

        ConsumingLinkedList<Integer> consumer = newConsumingLinkedList();

        consumer.accept(12);

        assertThat(consumer).isInstanceOf(LinkedList.class);
        assertThat(consumer.size()).isEqualTo(1);
        assertThat(consumer.getFirst()).isEqualTo(12);
    }

    @Test
    public void treeSetAggregatorWorks() {

        ConsumingTreeSet<Integer> consumer = newConsumingTreeSet();

        consumer.accept(13);
        consumer.accept(12);
        consumer.accept(15);

        assertThat(consumer).isInstanceOf(TreeSet.class);
        assertThat(consumer.size()).isEqualTo(3);
        assertThat(consumer.first()).isEqualTo(12);
        assertThat(consumer.last()).isEqualTo(15);
    }

    @Test
    public void priorityQueueAggregatorWorks() {

        ConsumingPriorityQueue<Integer> consumer = newConsumingPriorityQueue();

        consumer.accept(12);
        consumer.accept(12);
        consumer.accept(15);

        assertThat(consumer).isInstanceOf(PriorityQueue.class);
        assertThat(consumer.size()).isEqualTo(3);
        assertThat(consumer.poll()).isEqualTo(12);
        assertThat(consumer.poll()).isEqualTo(12);
        assertThat(consumer.poll()).isEqualTo(15);
        assertThat(consumer.isEmpty()).isTrue();
    }
}
