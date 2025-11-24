package io.github.jon1van.func;

import static io.github.jon1van.func.CompositeConsumer.combine;
import static org.assertj.core.api.Assertions.*;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

class CompositeConsumerTest {

    @Test
    void canCombineTwoIntConsumers() {

        StringBuffer sb = new StringBuffer();

        Consumer<Integer> intConsumer1 = (x) -> {
            sb.append("int1 " + x + "\n");
        };

        Consumer<Integer> intConsumer2 = (x) -> {
            sb.append("int2 " + x + "\n");
        };

        CompositeConsumer<Integer> compositeConsumer = new CompositeConsumer<>(intConsumer1, intConsumer2);

        compositeConsumer.accept(1);

        assertThat(sb.toString()).isEqualTo("int1 1\nint2 1\n");

        compositeConsumer.accept(2);

        assertThat(sb.toString()).isEqualTo("int1 1\nint2 1\nint1 2\nint2 2\n");
    }

    @Test
    void canCombineAnIntConsumerWithALongConsumer() {

        class Base {}
        class Subclass extends Base {}

        StringBuffer sb = new StringBuffer();

        Consumer<Base> baseConsumer = (x) -> sb.append("base");
        Consumer<Subclass> subclassConsumer = (x) -> sb.append("subclass");

        // Notice, when we combine these consumers we must use the narrowest type ...
        // For example, an instance of the base class may not be an instance of the subclass
        CompositeConsumer<Subclass> combo = combine(baseConsumer, subclassConsumer);

        combo.accept(new Subclass());

        assertThat(sb.toString()).isEqualTo("basesubclass");

        assertThat(combo.consumers().getFirst()).isEqualTo(baseConsumer);
        assertThat(combo.consumers().getLast()).isEqualTo(subclassConsumer);
    }
}
