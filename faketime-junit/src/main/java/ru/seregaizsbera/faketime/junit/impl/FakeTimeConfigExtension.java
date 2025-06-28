package ru.seregaizsbera.faketime.junit.impl;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import ru.seregaizsbera.faketime.FakeTime;
import ru.seregaizsbera.faketime.FakeTimeUniqueifier;
import ru.seregaizsbera.faketime.junit.FakeTimeConfig;

import java.time.ZoneId;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Тестовое расширение JUnit. Обрабатывает 1 конфигурацию теста и 1 вызов тестового метода.
 * Перед тестом устанавливает настройки времени. После теста сбрасывает настройки на значения по умолчанию.
 * @param timeConfig настройки времени для теста
 */
public record FakeTimeConfigExtension(FakeTimeConfig timeConfig) implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
    private static final String FAKE_TIME_STORE_KEY = "fakeTime";
    private static final String CURRENT_TIME_ZONE_STORE_KEY = "currentTimeZone";

    @Override
    public void beforeEach(ExtensionContext context) {
        var store = getStore(context);
        Optional.of(timeConfig)
                .map(FakeTimeConfig::timeZone)
                .filter(t -> !t.isEmpty())
                .map(ZoneId::of)
                .map(TimeZone::getTimeZone)
                .ifPresent(tz -> {
                    store.put(CURRENT_TIME_ZONE_STORE_KEY, TimeZone.getDefault());
                    TimeZone.setDefault(tz);
                });
        FakeTime fakeTime = FakeTime.install();
        store.put(FAKE_TIME_STORE_KEY, fakeTime);
        switch (timeConfig.mode()) {
            case FIXED -> fakeTime.fix();
            case FIXED_AT -> fakeTime.fixAt(timeConfig.epochUnit().toNanos(timeConfig.epoch()));
            case SHIFTED -> {
                fakeTime.shift(timeConfig.shift(), timeConfig.shiftUnit());
                fakeTime.setTick(timeConfig.tickUnit().toNanos(timeConfig.tick()));
            }
            case RESET -> {
                fakeTime.resetAt(timeConfig.epochUnit().toNanos(timeConfig.epoch()));
                fakeTime.setTick(timeConfig.tickUnit().toNanos(timeConfig.tick()));
            }
        }
        fakeTime.setAccelerator(timeConfig.accelerator().numerator(), timeConfig.accelerator().denominator());
        fakeTime.setUniqueifier(FakeTimeUniqueifier.valueOf(timeConfig.uniqueifier().name()));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        var store = getStore(context);
        Optional.of(CURRENT_TIME_ZONE_STORE_KEY)
                .map(tz -> store.get(tz, TimeZone.class))
                .ifPresent(TimeZone::setDefault);
        Optional.of(FAKE_TIME_STORE_KEY)
                .map(it -> store.get(it, FakeTime.class))
                .ifPresent(FakeTime::reset);
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(FakeTimeConfigExtension.class, context.getRequiredTestMethod()));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isInstance(timeConfig);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return timeConfig;
    }
}
