package org.mitre.caasd.commons.out;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mitre.caasd.commons.out.Sinks.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class SinksTest {

    @Test
    public void noOpSinkWorks() {

        OutputSink<Integer> sink = noOpSink();

        sink.accept(5);
        sink.accept(15);
    }

    @Test
    public void collectionSinkWorks() {

        CollectionSink<Integer> sink = collectionSink(newArrayList());

        sink.accept(5);
        sink.accept(15);

        List<Integer> col = (List<Integer>) sink.collection();

        assertThat(col.get(0), is(5));
        assertThat(col.get(1), is(15));
    }
}
