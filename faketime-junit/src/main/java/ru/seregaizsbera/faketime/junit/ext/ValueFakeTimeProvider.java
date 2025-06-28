package ru.seregaizsbera.faketime.junit.ext;

import org.junit.jupiter.api.extension.ExtensionContext;
import ru.seregaizsbera.faketime.junit.FakeTimeConfig;
import ru.seregaizsbera.faketime.junit.FakeTimeConfigMode;
import ru.seregaizsbera.faketime.junit.FakeTimeProvider;
import ru.seregaizsbera.faketime.junit.annotations.FakeTime;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Реализация источника настроек времени, получаемых из аннотаций {@link FakeTime}
 */
public class ValueFakeTimeProvider extends AnnotationBasedFakeTimeProvider<FakeTime> implements FakeTimeProvider {
    /**
     * Для модулей надо пустые конструкторы явно создавать
     */
    public ValueFakeTimeProvider() {
        // explicit default constructor
    }

    @Override
    protected Stream<FakeTimeConfig> getTime(ExtensionContext context, FakeTime annotation) {
        var result = makeFakeTimeConfig(annotation);
        return Stream.of(result);
    }

    public static FakeTimeConfig makeFakeTimeConfig(FakeTime annotation) {
        var builder = FakeTimeConfig.builder(annotation.name());
        long epoch;
        TimeUnit epochUnit;
        boolean dateTimeSpecified;
        if (annotation.dateTime() != null && !annotation.dateTime().isEmpty()) {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .parseStrict()
                    .parseCaseSensitive()
                    .appendPattern(annotation.dateTimeFormat())
                    .toFormatter(Locale.ROOT);
            Instant instant = formatter.parse(annotation.dateTime(), Instant::from);
            epoch = instant.getEpochSecond() * 1_000_000_000L + instant.getNano();
            epochUnit = TimeUnit.NANOSECONDS;
            dateTimeSpecified = true;
        } else {
            epoch = annotation.epoch();
            epochUnit = annotation.epochUnit();
            dateTimeSpecified = false;
        }
        switch (annotation.mode()) {
            case IMPLICIT -> {
                if (dateTimeSpecified) {
                    builder.mode(FakeTimeConfigMode.RESET).epoch(epoch, epochUnit);
                } else {
                    builder.mode(FakeTimeConfigMode.SHIFTED).shift(annotation.shift(), annotation.shiftUnit());
                }
            }
            case FIXED -> builder.mode(FakeTimeConfigMode.FIXED);
            case FIXED_AT -> builder.mode(FakeTimeConfigMode.FIXED_AT).epoch(epoch, epochUnit);
            case RESET -> builder.mode(FakeTimeConfigMode.RESET).epoch(epoch, epochUnit);
            case SHIFTED -> builder.mode(FakeTimeConfigMode.SHIFTED).shift(annotation.shift(), annotation.shiftUnit());
        }
        return builder.tick(annotation.tick(), annotation.tickUnit())
                .timeZone(annotation.timeZone())
                .accelerate(annotation.accelerator().numerator(), annotation.accelerator().denominator())
                .uniqueifier(annotation.uniqueifier())
                .build();
    }
}
