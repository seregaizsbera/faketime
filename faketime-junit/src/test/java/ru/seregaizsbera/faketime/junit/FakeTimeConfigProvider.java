package ru.seregaizsbera.faketime.junit;

import java.util.List;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static ru.seregaizsbera.faketime.junit.FakeTimeUniqueifier.UNIQUE_ATOMIC;

/**
 * Поставляет тестам настройки времени
 */
@SuppressWarnings("unused")
public class FakeTimeConfigProvider {
    public static List<FakeTimeConfig> fakeTime_testInstant() {
        return List.of(
                FakeTimeConfig.builder("Метод: март 2021-го").mode(FakeTimeConfigMode.RESET)
                        .instant(2021, 3, 8, 13, 43, 54, 0, "UTC")
                        .tick(1L, SECONDS)
                        .build(),
                FakeTimeConfig.builder("Метод: март 2020-го").mode(FakeTimeConfigMode.RESET)
                        .instant(2020, 3, 8, 13, 43, 54, 0, "UTC")
                        .tick(1L, SECONDS)
                        .build(),
                FakeTimeConfig.builder("Посекундно").mode(FakeTimeConfigMode.SHIFTED)
                        .tick(1L, SECONDS)
                        .uniqueifier(UNIQUE_ATOMIC)
                        .build()
        );
    }

    public static FakeTimeConfig fakeTime_testBlankTimeConfigName() {
        return FakeTimeConfig.builder()
                        .tick(17, SECONDS)
                        .build();
    }

    public static FakeTimeConfig[] fakeTime_testMethodArray() {
        return new FakeTimeConfig[] {
                FakeTimeConfig.builder("массив x1/2").mode(FakeTimeConfigMode.RESET)
                        .instant(2012, 12, 12, 12, 12, 12, 12, "UTC")
                        .accelerate(1L, 2L)
                        .build()
        };
    }

    public static Stream<FakeTimeConfig> fakeTime_testMethodStream() {
        return Stream.of(
                FakeTimeConfig.builder("стрим x2").mode(FakeTimeConfigMode.RESET)
                        .instant(2012, 12, 12, 12, 12, 12, 12, "UTC")
                        .accelerate(2L, 1L)
                        .build());
    }
}
