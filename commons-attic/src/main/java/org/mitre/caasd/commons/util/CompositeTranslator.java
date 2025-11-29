package org.mitre.caasd.commons.util;

/**
 * A CompositeTranslator combines two translators into a single translator.
 *
 * @param <A> The starting type
 * @param <B> The intermediate type (this type is only relevant when calling the constructor)
 * @param <C> The ending type
 */
public class CompositeTranslator<A, B, C> implements Translator<A, C> {

    private final Translator<A, B> step1;
    private final Translator<B, C> step2;

    public CompositeTranslator(Translator<A, B> step1, Translator<B, C> step2) {
        this.step1 = step1;
        this.step2 = step2;
    }

    @Override
    public C to(A item) {
        B middle = step1.to(item);
        C result = step2.to(middle);
        return result;
    }

    @Override
    public A from(C item) {
        B middle = step2.from(item);
        A result = step1.from(middle);
        return result;
    }
}
