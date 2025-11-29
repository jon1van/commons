package org.mitre.caasd.commons;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An InstantIterator iterates over a steadily increasing sequence of Instants that are all within a
 * fixed TimeWindow.
 */
public class InstantIterator implements Iterator<Instant> {

    private final TimeWindow window;

    private final Duration timeStep;

    private Instant next;

    /**
     * Create an InstantIterator that traverses time values in between these two times.
     *
     * @param startTime The first instant in the iteration
     * @param endTime   The last Instant in the iteration
     * @param timeStep  The size of the steps between the jumps (must be a positive Duration)
     */
    public InstantIterator(Instant startTime, Instant endTime, Duration timeStep) {
        checkNotNull(startTime, "The start time cannot be null");
        checkNotNull(endTime, "The end time cannot be null");
        checkNotNull(timeStep, "The timeStep cannot be null");
        checkArgument(!timeStep.isNegative(), "The timeStep cannot be negative");
        checkArgument(!timeStep.isZero(), "The timeStep cannot be zero");
        this.window = new TimeWindow(startTime, endTime);
        this.timeStep = timeStep;
        this.next = window.start();
    }

    public InstantIterator(TimeWindow window, Duration timeStep) {
        this(window.start(), window.end(), timeStep);
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Instant next() {
        if (hasNext()) {
            Instant returnMe = next;
            updateNext();
            return returnMe;
        } else {
            throw new NoSuchElementException();
        }
    }

    private void updateNext() {

        if (next.equals(window.end())) {
            next = null;
        } else {
            Instant possible = next.plus(timeStep);

            next = (window.contains(possible)) ? possible : window.end();
        }
    }
}
