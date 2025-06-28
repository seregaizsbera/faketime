package ru.seregaizsbera.faketime.junit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.seregaizsbera.faketime.junit.annotations.FakeTime;
import ru.seregaizsbera.faketime.junit.annotations.FakeTimeMethod;
import ru.seregaizsbera.faketime.junit.annotations.FakeTimeTest;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.seregaizsbera.faketime.junit.FakeTimeMode.FIXED;
import static ru.seregaizsbera.faketime.junit.FakeTimeMode.FIXED_AT;
import static ru.seregaizsbera.faketime.junit.FakeTimeUniqueifier.UNIQUE_ATOMIC;

class AnnotationsTest {
    private DateTimeFormatter dateTimeFormatter;

    @BeforeEach
    void setUp() {
        dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                .withLocale(Locale.ROOT)
                .withZone(ZoneOffset.UTC);
    }

    @Test
    @FakeTime(dateTime = "2019-02-22T04:00:34.123+03:00")
    void testSingleAnnotation() {
        IntStream.range(0, 10).forEach(i -> Awaitility.await()
                .pollDelay(50L, MILLISECONDS)
                .until(() -> {
                    assertThat(ZonedDateTime.now(ZoneOffset.UTC).getYear()).isEqualTo(2019);
                    return true;
                }));
        assertThat(System.currentTimeMillis()).isPositive();
    }

    @FakeTimeTest
    @FakeTime(name = "Стандартное системное время без изменений")
    @FakeTime(dateTime = "2012-12-12T23:59:57.123Z",           name = "Полночь UTC")
    @FakeTime(dateTime = "2012-12-12T23:59:57.123+03:00",      name = "Полночь MSK")
    @FakeTime(dateTime = "2012-12-12T22:30:55.123Z",           name = "Конец суток UTC")
    @FakeTime(dateTime = "2012-12-12T01:30:55.123Z",           name = "Начало суток UTC")
    @FakeTime(dateTime = "2012-12-12T22:30:55.123+03:00",      name = "Конец суток MSK")
    @FakeTime(dateTime = "2012-12-12T01:30:55.123+03:00",      name = "Начало суток MSK")
    @FakeTime(tick = 10L, tickUnit = MILLISECONDS,             name = "По 10 миллисекунд")
    @FakeTime(            tickUnit = SECONDS,                  name = "По 1 секунде")
    @FakeTime(uniqueifier = UNIQUE_ATOMIC, tickUnit = SECONDS, name = "Уникально по 1 секунде")
    @FakeTime(mode = FIXED_AT, dateTime = "2012-12-12T01:30:55.123+03:00", name = "Время остановлено в 2012-м году")
    @FakeTime(mode = FIXED,                                    name = "Время остановлено")
    @FakeTime(timeZone = "UTC",                                name = "Таймзона UTC")
    @FakeTime(timeZone = "Europe/Moscow",                      name = "Таймзона Europe/Moscow")
    @FakeTime(timeZone = "America/Ciudad_Juarez",              name = "Экзотическая таймзона")
    @FakeTimeMethod(declaring = FakeTimeConfigProvider.class)
    void testInstant(FakeTimeConfig time) {
        showTime(time);
        assertThat(time.name()).isNotBlank();
    }

    @FakeTimeTest
    @FakeTimeMethod(declaring = FakeTimeConfigProvider.class)
    void testBlankTimeConfigName(FakeTimeConfig time) {
        assertThat(time.name()).isBlank();
    }

    @FakeTimeTest
    @FakeTimeMethod(declaring = FakeTimeConfigProvider.class)
    void testMethodArray(FakeTimeConfig time) {
        showTime(time);
        assertThat(time.name()).isNotBlank();
    }

    @FakeTimeTest
    @FakeTimeMethod(declaring = FakeTimeConfigProvider.class)
    void testMethodStream(FakeTimeConfig time) {
        showTime(time);
        assertThat(time.name()).isNotBlank();        assertThat(time.name()).isNotBlank();
    }

    private void showTime(FakeTimeConfig time) {
        IntStream.range(0, 10).forEach(i -> {
            System.out.printf("%s %s (%s)%n", dateTimeFormatter.format(ZonedDateTime.now()), time.name(), "haea");
            Awaitility.await()
                    .pollDelay(50L, MILLISECONDS)
                    .until(() -> true);
        });
    }
}
