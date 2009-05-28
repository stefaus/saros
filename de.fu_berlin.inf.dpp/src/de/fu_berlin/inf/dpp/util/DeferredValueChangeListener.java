package de.fu_berlin.inf.dpp.util;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeferredValueChangeListener<T> implements ValueChangeListener<T> {

    ValueChangeListener<T> wrapped;

    ScheduledExecutorService executor;

    Future<?> setFilterFuture;

    long time;

    TimeUnit timeUnit;

    public static <S> ValueChangeListener<S> defer(ValueChangeListener<S> wrap,
        long time, TimeUnit timeUnit) {
        return new DeferredValueChangeListener<S>(wrap, time, timeUnit);
    }

    public DeferredValueChangeListener(ValueChangeListener<T> wrapped) {
        this(wrapped, 1, TimeUnit.SECONDS);
    }

    public DeferredValueChangeListener(ValueChangeListener<T> wrapped,
        long time, TimeUnit timeUnit) {
        this.wrapped = wrapped;
        this.time = time;
        this.timeUnit = timeUnit;
    }

    public void setValue(final T newValue) {

        // Lazyly create Thread-Pool Executor
        if (executor == null)
            executor = Executors.newSingleThreadScheduledExecutor();

        // Cancel previous set operations
        if (setFilterFuture != null && !setFilterFuture.isCancelled())
            setFilterFuture.cancel(false);

        setFilterFuture = executor.schedule(new Runnable() {
            public void run() {
                wrapped.setValue(newValue);
            }
        }, time, timeUnit);
    }
}
