package io.github.jon1van.collect;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.jon1van.collect.HashedLinkedSequence.newHashedLinkedSequence;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

public class HashedLinkedSequenceTest {

    @Test
    public void testBasicUsage() {

        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(5);
        list.add(12);
        list.add(13);

        assertThat(list.size()).isEqualTo(3);

        assertThat(list.getFirst()).isEqualTo(5);
        assertThat(list.getLast()).isEqualTo(13);

        assertThat(list.getElementAfter(5)).isEqualTo(12);
        assertThat(list.getElementBefore(13)).isEqualTo(12);

        assertThat(list.contains(5)).isTrue();
        assertThat(list.contains(12)).isTrue();
        assertThat(list.contains(13)).isTrue();
    }

    @Test
    public void firstItemIsFirstAndLast() {
        HashedLinkedSequence<Integer> list = newHashedLinkedSequence();
        list.add(5);
        assertThat(list.getFirst()).isEqualTo(5);
        assertThat(list.getLast()).isEqualTo(5);
    }

    @Test
    public void testAddFirst_null() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();

        assertThrows(NullPointerException.class, () -> list.addFirst(null));
    }

    @Test
    public void testAddFirst_duplicateItem() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(5);

        // duplicate should fail
        assertThrows(IllegalArgumentException.class, () -> list.addFirst(5));
    }

    @Test
    public void addFirstToEmptyListWorks() {
        HashedLinkedSequence<Integer> list = newHashedLinkedSequence();
        list.addFirst(12);
        assertThat(list.getFirst()).isEqualTo(12);
        assertThat(list.getLast()).isEqualTo(12);
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.isEmpty()).isFalse();
    }

    @Test
    public void testAddFirst_happyPath() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(12);
        list.addFirst(1);

        assertThat(list.getFirst()).isEqualTo(1);
        assertThat(list.getLast()).isEqualTo(12);
        assertThat(list.size()).isEqualTo(2);

        assertThat(list.getElementAfter(1)).isEqualTo(12);
        assertThat(list.getElementBefore(12)).isEqualTo(1);
    }

    @Test
    public void testGetElementAfter_itemNotInList() {

        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(5);

        // 12 is not in the list -- so this call is undefined
        assertThrows(IllegalArgumentException.class, () -> list.getElementAfter(12));
    }

    @Test
    public void testGetElementAfter_atEndOfList() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(5);

        // at the end of the list, nothing to get
        assertThrows(NoSuchElementException.class, () -> list.getElementAfter(5));
    }

    @Test
    public void testGetElementAfter() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(5);
        list.add(22);
        assertThat(list.getElementAfter(5)).isEqualTo(22);
    }

    @Test
    public void testGetElementBefore_itemNotInList() {

        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(5);

        // 12 is not in the list -- so this call is undefined
        assertThrows(IllegalArgumentException.class, () -> list.getElementBefore(12));
    }

    @Test
    public void testGetElementBefore_atFrontOfList() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(5);

        // at the front of the list, nothing to get
        assertThrows(NoSuchElementException.class, () -> list.getElementBefore(5));
    }

    @Test
    public void testGetElementBefore() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(5);
        list.add(22);
        assertThat(list.getElementBefore(22)).isEqualTo(5);
    }

    @Test
    public void testInsertAfter_referenceDoesNotExist() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();

        // 12 does not exist
        assertThrows(IllegalArgumentException.class, () -> list.insertAfter(5, 12));
    }

    @Test
    public void testInsertAfter_alreadyPresent() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(5);

        // 5 already exists
        assertThrows(IllegalArgumentException.class, () -> list.insertAfter(5, 5));
    }

    @Test
    public void testInsertAfter_happyPath() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(5);
        list.insertAfter(12, 5);

        assertThat(list.getFirst()).isEqualTo(5);
        assertThat(list.getLast()).isEqualTo(12);
        assertThat(list.getElementAfter(5)).isEqualTo(12);
        assertThat(list.size()).isEqualTo(2);

        assertThat(list.contains(12)).isTrue();
    }

    @Test
    public void insertAfter_betweenTwoItems() {
        HashedLinkedSequence<Integer> list = newHashedLinkedSequence(1, 2);
        list.insertAfter(12, 1);

        assertThat(list.getFirst()).isEqualTo(1);
        assertThat(list.getLast()).isEqualTo(2);

        assertThat(list.getElementAfter(1)).isEqualTo(12);
        assertThat(list.getElementBefore(2)).isEqualTo(12);
        assertThat(list.size()).isEqualTo(3);

        assertThat(list.contains(12)).isTrue();
    }

    @Test
    public void insertAfterLastElementOccursProperly() {
        HashedLinkedSequence<Integer> list = newHashedLinkedSequence(1, 2, 3);
        list.insertAfter(22, 3);
        assertThat(list.size()).isEqualTo(4);
        assertThat(list.getLast()).isEqualTo(22);
        assertThat(list.getElementAfter(3)).isEqualTo(22);
        assertThat(list.getElementBefore(22)).isEqualTo(3);
    }

    @Test
    public void testInsertBefore_referenceDoesNotExist() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();

        // 12 does not exist
        assertThrows(IllegalArgumentException.class, () -> list.insertBefore(5, 12));
    }

    @Test
    public void testInsertBefore_alreadyPresent() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(5);

        // 5 already exists
        assertThrows(IllegalArgumentException.class, () -> list.insertBefore(5, 5));
    }

    @Test
    public void insertBeforeFirstElementWorks() {

        HashedLinkedSequence<Integer> list = newHashedLinkedSequence(1, 2);
        list.insertBefore(22, 1); // should get 22, 1, 2

        assertThat(list.size()).isEqualTo(3);

        assertThat(list.getFirst()).isEqualTo(22);
        assertThat(list.getLast()).isEqualTo(2);

        assertThat(list.getElementAfter(22)).isEqualTo(1);
        assertThat(list.getElementAfter(1)).isEqualTo(2);

        assertThat(list.getElementBefore(2)).isEqualTo(1);
        assertThat(list.getElementBefore(1)).isEqualTo(22);
    }

    @Test
    public void testInsertBefore_happyPath() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(1);
        list.add(5);
        list.insertBefore(12, 5); // put 12 in between 1 and 5

        assertThat(list.getFirst()).isEqualTo(1);
        assertThat(list.getLast()).isEqualTo(5);
        assertThat(list.getElementAfter(1)).isEqualTo(12);
        assertThat(list.getElementBefore(5)).isEqualTo(12);

        assertThat(list.size()).isEqualTo(3);

        assertThat(list.contains(12)).isTrue();
    }

    @Test
    public void testRemove_first() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(1);
        list.add(2);
        list.add(3);

        boolean result = list.remove(1);

        assertThat(result).isTrue();
        assertThat(list.size()).isEqualTo(2);

        assertThat(list.getFirst()).isEqualTo(2);

        assertThat(list.getLast()).isEqualTo(3);

        assertThat(list.getElementAfter(2)).isEqualTo(3);

        assertThat(list.getElementBefore(3)).isEqualTo(2);

        assertThat(list.contains(1)).isFalse();
    }

    @Test
    public void testRemove_last() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(1);
        list.add(2);
        list.add(3);

        boolean result = list.remove(3);

        assertThat(result).isTrue();
        assertThat(list.size()).isEqualTo(2);

        assertThat(list.getFirst()).isEqualTo(1);
        assertThat(list.getLast()).isEqualTo(2);

        assertThat(list.getElementAfter(1)).isEqualTo(2);

        assertThat(list.getElementBefore(2)).isEqualTo(1);

        assertThat(list.contains(3)).isFalse();
    }

    @Test
    public void testRemove_middle() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(1);
        list.add(2);
        list.add(3);

        boolean result = list.remove(2);

        assertThat(result).isTrue();
        assertThat(list.size()).isEqualTo(2);

        assertThat(list.getFirst()).isEqualTo(1);
        assertThat(list.getLast()).isEqualTo(3);

        assertThat(list.getElementAfter(1)).isEqualTo(3);

        assertThat(list.getElementBefore(3)).isEqualTo(1);

        assertThat(list.contains(2)).isFalse();
    }

    @Test
    public void removingLastItemLeavesEmptyList() {
        HashedLinkedSequence<Integer> list = newHashedLinkedSequence(22);
        assertThat(list.contains(22)).isTrue();
        assertThat(list.isEmpty()).isFalse();

        list.remove(22);

        assertThat(list.contains(22)).isFalse();
        assertThat(list.isEmpty()).isTrue();

        assertThrows(NoSuchElementException.class, () -> list.getFirst());
        assertThrows(NoSuchElementException.class, () -> list.getLast());
    }

    @Test
    public void testRemove_miss() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(1);
        list.add(2);
        list.add(3);

        boolean result = list.remove(55);

        assertThat(result).isFalse();
        assertThat(list.size()).isEqualTo(3);

        assertThat(list.getFirst()).isEqualTo(1);
        assertThat(list.getLast()).isEqualTo(3);
    }

    @Test
    public void iteratorProvidesAWorkingIterator_emptyList() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        Iterator<Integer> iter = list.iterator();

        assertThat(iter.hasNext()).isFalse();
    }

    @Test
    public void iteratorProvidesAWorkingIterator_whenDataIsThere() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(1);
        list.add(2);
        list.add(3);

        Iterator<Integer> iter = list.iterator();

        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(1);

        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(2);

        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(3);

        assertThat(iter.hasNext()).isFalse();
    }

    @Test
    public void iteratorThrowsNoSuchElementException() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        Iterator<Integer> iter = list.iterator();

        assertThat(iter.hasNext()).isFalse();

        assertThrows(NoSuchElementException.class, () -> iter.next());
    }

    @Test
    public void iteratorThrowsConcurrentModification_add() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(1);
        list.add(2);

        Iterator<Integer> iter = list.iterator();

        list.add(22);

        // breaks because the list was editted after the iterator was made
        assertThrows(ConcurrentModificationException.class, () -> iter.next());
    }

    @Test
    public void iteratorThrowsConcurrentModification_addLast() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(1);
        list.add(2);

        Iterator<Integer> iter = list.iterator();

        list.addLast(22);

        // breaks because the list was editted after the iterator was made
        assertThrows(ConcurrentModificationException.class, () -> iter.next());
    }

    @Test
    public void iteratorThrowsConcurrentModification_addFirst() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(1);
        list.add(2);

        Iterator<Integer> iter = list.iterator();

        list.addFirst(22);

        // breaks because the list was editted after the iterator was made
        assertThrows(ConcurrentModificationException.class, () -> iter.next());
    }

    @Test
    public void iteratorThrowsConcurrentModification_remove() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(1);
        list.add(2);

        Iterator<Integer> iter = list.iterator();

        list.remove(1);

        // breaks because the list was editted after the iterator was made
        assertThrows(ConcurrentModificationException.class, () -> iter.next());
    }

    @Test
    public void iteratorThrowsConcurrentModification_insertBefore() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(1);
        list.add(2);

        Iterator<Integer> iter = list.iterator();

        list.insertBefore(101, 1);

        // breaks because the list was editted after the iterator was made
        assertThrows(ConcurrentModificationException.class, () -> iter.next());
    }

    @Test
    public void iteratorThrowsConcurrentModification_insertAfter() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(1);
        list.add(2);

        Iterator<Integer> iter = list.iterator();

        list.insertAfter(101, 1);

        // breaks because the list was editted after the iterator was made
        assertThrows(ConcurrentModificationException.class, () -> iter.next());
    }

    @Test
    public void iteratorDoesNotFailsWhenRemoveDoesNothing() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(1);
        list.add(2);

        Iterator<Integer> iter = list.iterator();

        list.remove(22);

        // remove did nothing, no ConcurrentModificationException should be thrown
        assertDoesNotThrow(() -> iter.next());
    }

    @Test
    public void collectionsToArrayMethodWorks() {

        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(1);
        list.add(2);
        list.add(5);

        Object[] array = list.toArray();
        assertThat(array.length).isEqualTo(3);

        assertThat((Integer) array[0]).isEqualTo(1);
        assertThat((Integer) array[1]).isEqualTo(2);

        assertThat((Integer) array[2]).isEqualTo(5);
    }

    @Test
    public void collectionTypeToArrayMethodWorks() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(1);
        list.add(2);
        list.add(5);
        Integer[] array = list.toArray(new Integer[] {});
        assertThat(array.length).isEqualTo(3);

        assertThat(array[0]).isEqualTo(1);
        assertThat(array[1]).isEqualTo(2);

        assertThat(array[2]).isEqualTo(5);
    }

    @Test
    public void contains_happyPath() {

        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(1);
        list.add(2);
        list.add(5);

        assertThat(list.contains(0)).isFalse();
        assertThat(list.contains(1)).isTrue();
        assertThat(list.contains(2)).isTrue();
        assertThat(list.contains(3)).isFalse();
        assertThat(list.contains(4)).isFalse();
        assertThat(list.contains(5)).isTrue();
        assertThat(list.contains(6)).isFalse();
    }

    @Test
    public void contains_badInput() {
        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        list.add(1);
        list.add(2);
        list.add(5);

        assertThat(list.contains(null)).isFalse();
        assertThat(list.contains("hello")).isFalse();
    }

    @Test
    public void addAll_addsData() {

        HashedLinkedSequence<Integer> list = new HashedLinkedSequence<>();
        boolean result = list.addAll(newArrayList(1, 2, 5));

        assertThat(list.contains(1)).isTrue();
        assertThat(list.contains(2)).isTrue();
        assertThat(list.contains(5)).isTrue();
        assertThat(list.size() == 3).isTrue();
        assertThat(result).isTrue();
    }

    @Test
    public void arrayFactoryMethodWorks() {
        HashedLinkedSequence<Integer> list = newHashedLinkedSequence(1, 6, 12, 22);

        assertThat(list.contains(1)).isTrue();
        assertThat(list.contains(6)).isTrue();
        assertThat(list.contains(12)).isTrue();
        assertThat(list.contains(22)).isTrue();
        assertThat(list.size() == 4).isTrue();
        assertThat(list.getFirst()).isEqualTo(1);
        assertThat(list.getLast()).isEqualTo(22);
        assertThat(list.getElementAfter(1)).isEqualTo(6);
        assertThat(list.getElementAfter(6)).isEqualTo(12);
        assertThat(list.getElementAfter(12)).isEqualTo(22);
        assertThat(list.getElementBefore(22)).isEqualTo(12);
        assertThat(list.getElementBefore(12)).isEqualTo(6);
        assertThat(list.getElementBefore(6)).isEqualTo(1);
    }

    @Test
    public void iterableFactoryMethodWorks() {
        HashedLinkedSequence<Integer> list = newHashedLinkedSequence(
                newArrayList(1, 6, 12, 22) // this an iterable
                );

        assertThat(list.contains(1)).isTrue();
        assertThat(list.contains(6)).isTrue();
        assertThat(list.contains(12)).isTrue();
        assertThat(list.contains(22)).isTrue();
        assertThat(list.size() == 4).isTrue();
        assertThat(list.getFirst()).isEqualTo(1);
        assertThat(list.getLast()).isEqualTo(22);
        assertThat(list.getElementAfter(1)).isEqualTo(6);
        assertThat(list.getElementAfter(6)).isEqualTo(12);
        assertThat(list.getElementAfter(12)).isEqualTo(22);
        assertThat(list.getElementBefore(22)).isEqualTo(12);
        assertThat(list.getElementBefore(12)).isEqualTo(6);
        assertThat(list.getElementBefore(6)).isEqualTo(1);
    }

    @Test
    public void clearRemovesAllData() {

        HashedLinkedSequence<Integer> list = newHashedLinkedSequence(1, 6, 12, 22);

        assertThat(list.contains(1)).isTrue();
        assertThat(list.contains(6)).isTrue();
        assertThat(list.contains(12)).isTrue();
        assertThat(list.contains(22)).isTrue();
        assertThat(list.size() == 4).isTrue();
        assertThat(list.isEmpty()).isFalse();

        list.clear(); // apply clear

        assertThat(list.contains(1)).isFalse();
        assertThat(list.contains(6)).isFalse();
        assertThat(list.contains(12)).isFalse();
        assertThat(list.contains(22)).isFalse();
        assertThat(list.size() == 0).isTrue();
        assertThat(list.isEmpty()).isTrue();

        assertThrows(NoSuchElementException.class, () -> list.getFirst());
        assertThrows(NoSuchElementException.class, () -> list.getLast());
    }

    @Test
    public void containsAll_worksAsExpected() {
        HashedLinkedSequence<Integer> list = newHashedLinkedSequence(1, 2, 3, 4);

        assertThat(list.containsAll(newArrayList(1, 2, 3, 4))).isTrue();
        assertThat(list.containsAll(newArrayList(1, 2, 3))).isTrue();
        assertThat(list.containsAll(newArrayList(1, 2))).isTrue();
        assertThat(list.containsAll(newArrayList(1))).isTrue();

        assertThat(list.containsAll(newArrayList(1, 2, 5))).isFalse();
        assertThat(list.containsAll(newArrayList(5))).isFalse();
        assertThat(list.containsAll(newArrayList("String"))).isFalse();
    }

    @Test
    public void removeAll_worksAsExpected() {

        HashedLinkedSequence<Integer> list = newHashedLinkedSequence(1, 2, 3, 4, 5, 6);

        boolean modified = list.removeAll(newArrayList("String1", "String2"));
        assertThat(modified).isFalse();

        assertThat(list.size() == 6).isTrue();

        modified = list.removeAll(newArrayList(2, 4, 6, 22));
        assertThat(modified).isTrue();

        assertThat(list.contains(1)).isTrue();
        assertThat(list.contains(2)).isFalse();
        assertThat(list.contains(3)).isTrue();
        assertThat(list.contains(4)).isFalse();
        assertThat(list.contains(5)).isTrue();
        assertThat(list.contains(6)).isFalse();

        assertThat(list.size()).isEqualTo(3);

        assertThat(list.getFirst()).isEqualTo(1);
        assertThat(list.getLast()).isEqualTo(5);
        assertThat(list.getElementAfter(1)).isEqualTo(3);

        assertThat(list.getElementAfter(3)).isEqualTo(5);
        assertThat(list.getElementBefore(5)).isEqualTo(3);

        assertThat(list.getElementBefore(3)).isEqualTo(1);
    }

    @Test
    public void removeAllWorksWhenGivenBigCollectionToRemove() {

        HashedLinkedSequence<Integer> seq = newHashedLinkedSequence(1, 2, 3);

        boolean modified = seq.removeAll(newArrayList(2, 4, 6, 22, 35));

        assertThat(modified).isTrue();
        assertThat(seq.contains(2)).isFalse();
        assertThat(seq.size()).isEqualTo(2);
    }

    @Test
    public void retainAllWorksAsExpect() {

        HashedLinkedSequence<Integer> seq = newHashedLinkedSequence(1, 2, 3);

        boolean modified = seq.retainAll(newArrayList(2, 4, 6, 22, 35));

        assertThat(modified).isTrue();
        assertThat(seq.contains(1)).isFalse();
        assertThat(seq.contains(2)).isTrue();
        assertThat(seq.contains(3)).isFalse();
        assertThat(seq.size()).isEqualTo(1);
    }

    @Test
    public void removingFirstElementViaIteratorWorks() {
        HashedLinkedSequence<Integer> seq = newHashedLinkedSequence(1, 2, 3);
        Iterator<Integer> iter = seq.iterator();
        assertThat(seq.size() == 3).isTrue();

        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(1);

        iter.remove();

        assertThat(seq.getFirst()).isEqualTo(2);

        assertThat(seq.getLast()).isEqualTo(3);

        assertThat(seq.getElementAfter(2)).isEqualTo(3);

        assertThat(seq.getElementBefore(3)).isEqualTo(2);

        assertThat(seq.contains(1)).isFalse();
        assertThat(seq.size()).isEqualTo(2);

        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(2);
    }

    @Test
    public void removingLastElementViaIteratorWorks() {
        HashedLinkedSequence<Integer> seq = newHashedLinkedSequence(1, 2, 3);
        Iterator<Integer> iter = seq.iterator();
        assertThat(seq.size() == 3).isTrue();

        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(1);
        assertThat(iter.next()).isEqualTo(2);
        assertThat(iter.next()).isEqualTo(3);

        iter.remove();

        assertThat(seq.getFirst()).isEqualTo(1);
        assertThat(seq.getLast()).isEqualTo(2);

        assertThat(seq.getElementAfter(1)).isEqualTo(2);

        assertThat(seq.getElementBefore(2)).isEqualTo(1);
        assertThat(seq.contains(3)).isFalse();
        assertThat(seq.size()).isEqualTo(2);

        assertThat(iter.hasNext()).isFalse();
    }

    @Test
    public void removingMiddleElementViaIteratorWorks() {
        HashedLinkedSequence<Integer> seq = newHashedLinkedSequence(1, 2, 3);
        Iterator<Integer> iter = seq.iterator();
        assertThat(seq.size() == 3).isTrue();

        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(1);
        assertThat(iter.next()).isEqualTo(2);

        iter.remove();

        assertThat(seq.getFirst()).isEqualTo(1);
        assertThat(seq.getLast()).isEqualTo(3);
        assertThat(seq.getElementAfter(1)).isEqualTo(3);
        assertThat(seq.getElementBefore(3)).isEqualTo(1);
        assertThat(seq.contains(2)).isFalse();
        assertThat(seq.size()).isEqualTo(2);

        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(3);
    }

    @Test
    public void removingOnlyElementViaIteratorWorks() {
        HashedLinkedSequence<Integer> seq = newHashedLinkedSequence(1);
        Iterator<Integer> iter = seq.iterator();
        assertThat(seq.size() == 1).isTrue();

        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(1);

        iter.remove();

        assertThat(seq.contains(1)).isFalse();
        assertThat(seq.isEmpty()).isTrue();

        assertThat(iter.hasNext()).isFalse();
    }
}
