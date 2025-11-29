package org.mitre.caasd.commons.util;

/**
 * A Translator converts Objects of one type to another type <B>and back </B>.
 */
public interface Translator<A, B> {

    B to(A item);

    A from(B item);

    default <C> Translator<A, C> compose(Translator<B, C> step2) {
        return new CompositeTranslator(this, step2);
    }
}
