package ru.seregaizsbera.faketime.impl;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import ru.seregaizsbera.faketime.FakeTime;
import ru.seregaizsbera.faketime.FakeTimeUniqueifier;
import ru.seregaizsbera.faketime.internal.TimeInterceptor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.instrument.Instrumentation;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Реализация API модуля
 */
public final class FakeTimeInternal implements FakeTime {
    private static final long NANOS_PER_SEC = 1_000_000_000L;
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Supplier<FakeTime> initial = FakeTimeInternal::initial;
    private static Supplier<FakeTime> getter = initial;
    private FakeTimeInternal() {}

    public static FakeTime get() {
        return getter.get();
    }

    private static FakeTime initial() {
        try {
            if (!lock.tryLock(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Не удалось дождаться блокировки (igzv)");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Процесс прерван (ldlt)");
        }
        try {
            if (getter != initial) {
                return getter.get();
            }
            var result = install();
            getter = () -> result;
            return result;
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("resource")
    private static FakeTime install() {
        Instrumentation instrumentation = ByteBuddyAgent.install();
        try (var jar = URLJarFile.getJarFile(FakeTimeInternal.class.getResource("/interceptors.jar"))) {
            instrumentation.appendToBootstrapClassLoaderSearch(jar);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        ByteBuddy byteBuddy = new ByteBuddy();
        byteBuddy.redefine(System.class)
                .method(ElementMatchers.named("currentTimeMillis").and(ElementMatchers.returns(long.class)))
                .intercept(MethodDelegation.to(TimeInterceptor.class))
                .make()
                .load(System.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
        byteBuddy.redefine(Clock.class)
                .method(ElementMatchers.named("currentInstant").and(ElementMatchers.returns(Instant.class)))
                .intercept(MethodDelegation.to(TimeInterceptor.class))
                .make()
                .load(Clock.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
        return new FakeTimeInternal();
    }

    @Override
    public void reset() {
        TimeInterceptor.reset();
    }

    @Override
    public void fix() {
        TimeInterceptor.fix();
    }

    @Override
    public void fixAt(long epochNs) {
        TimeInterceptor.fixAt(epochNs);
    }

    @Override
    public void fixAt(Instant moment) {
        TimeInterceptor.fixAt(moment.getEpochSecond() * NANOS_PER_SEC + moment.getNano());
    }

    @Override
    public void fixAt(long epochSec, int nanoOfSecond) {
        TimeInterceptor.fixAt(epochSec * NANOS_PER_SEC + nanoOfSecond);
    }

    @Override
    public void shift(long shiftNs) {
        TimeInterceptor.shift(shiftNs);
    }

    @Override
    public void shift(Duration shift) {
        TimeInterceptor.shift(shift.toNanos());
    }

    @Override
    public void shift(long shift, TimeUnit unit) {
        TimeInterceptor.shift(unit.toNanos(shift));
    }

    @Override
    public void resetAt(long epochNs) {
        TimeInterceptor.resetAt(epochNs);
    }

    @Override
    public void resetAt(Instant moment) {
        TimeInterceptor.resetAt(moment.getEpochSecond() * NANOS_PER_SEC + moment.getNano());
    }

    @Override
    public void resetAt(long epochSec, int nanoOfSecond) {
        TimeInterceptor.resetAt(epochSec * NANOS_PER_SEC + nanoOfSecond);
    }

    @Override
    public void setTick(long tickNs) {
        TimeInterceptor.setTick(tickNs);
    }

    @Override
    public void setTick(Duration tick) {
        TimeInterceptor.setTick(tick.toNanos());
    }

    @Override
    public void resetTick() {
        TimeInterceptor.setTick(1L);
    }

    @Override
    public void setAccelerator(long numerator, long denominator) {
        TimeInterceptor.setAccelerator(numerator, denominator);
    }

    @Override
    public void resetAccelerator() {
        TimeInterceptor.setAccelerator(1L, 1L);
    }

    @Override
    public void setUniqueifier(FakeTimeUniqueifier uniqueifier) {
        TimeInterceptor.setUniqueifier(TimeInterceptor.Uniqueifier.valueOf(uniqueifier.name()));
    }

    @Override
    public void resetUniqueifier() {
        setUniqueifier(FakeTimeUniqueifier.NOTHING);
    }
}
