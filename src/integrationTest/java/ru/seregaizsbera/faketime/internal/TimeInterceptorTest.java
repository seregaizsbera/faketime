package ru.seregaizsbera.faketime.internal;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("java:S5960")
class TimeInterceptorTest {
    @Test
    void testTimeInterceptor() {
        TimeInterceptor.fix();
        Instant time = TimeInterceptor.currentInstant();
        IntStream.range(0, 10).forEach(i -> Awaitility.await()
                .pollDelay(50L, TimeUnit.MILLISECONDS)
                .until(() -> {
                    assertThat(TimeInterceptor.currentInstant()).isEqualTo(time);
                    return true;
                }));
        Instant time2 = ZonedDateTime.of(2019, 12, 12, 12, 12, 12, 12, ZoneOffset.UTC).toInstant();
        TimeInterceptor.fixAt(time2.getEpochSecond() * 1_000_000_000L + time2.getNano());
        assertThat(TimeInterceptor.currentInstant().atZone(ZoneOffset.UTC).getYear()).isEqualTo(2019);
        IntStream.range(0, 10).forEach(i -> Awaitility.await()
                .pollDelay(50L, TimeUnit.MILLISECONDS)
                .until(() -> {
                    assertThat(TimeInterceptor.currentInstant()).isEqualTo(time2);
                    return true;
                }));
        long time3 = TimeInterceptor.currentTimeMillis();
        TimeInterceptor.shift(TimeUnit.SECONDS.toNanos(60L));
        long time4 = TimeInterceptor.currentTimeMillis();
        assertThat(time4 - time3).isGreaterThanOrEqualTo(60_000L);
        TimeInterceptor.resetAt(time2.getEpochSecond() * 1_000_000_000L + time2.getNano());
        assertThat(TimeInterceptor.currentInstant().atZone(ZoneOffset.UTC).getYear()).isEqualTo(2019);
        TimeInterceptor.reset();
        TimeInterceptor.setTick(0L);
        TimeInterceptor.setTick(1L);
        TimeInterceptor.setTick(-TimeUnit.SECONDS.toNanos(1L));
        IntStream.range(0, 10).forEach(i -> Awaitility.await()
                .pollDelay(50L, TimeUnit.MILLISECONDS)
                .until(() -> {
                    assertThat(TimeInterceptor.currentTimeMillis() % 1000L).isZero();
                    return true;
                }));
        TimeInterceptor.setTick(TimeUnit.SECONDS.toNanos(1L));
        IntStream.range(0, 10).forEach(i -> Awaitility.await()
                .pollDelay(50L, TimeUnit.MILLISECONDS)
                .until(() -> {
                    assertThat(TimeInterceptor.currentTimeMillis() % 1000L).isZero();
                    return true;
                }));
        TimeInterceptor.setUniqueifier(TimeInterceptor.Uniqueifier.NOTHING);
        TimeInterceptor.setUniqueifier(TimeInterceptor.Uniqueifier.UNIQUE_ATOMIC);
        long time5 = TimeInterceptor.currentTimeMillis();
        IntStream.range(0, 10).forEach(i -> Awaitility.await()
                .pollDelay(50L, TimeUnit.MILLISECONDS)
                .until(() -> {
                    assertThat(TimeInterceptor.currentTimeMillis() % 1000L).isZero();
                    return true;
                }));
        long time6 = TimeInterceptor.currentTimeMillis();
        assertThat((time6 - time5)).isEqualTo(11_000L);
        TimeInterceptor.setUniqueifier(TimeInterceptor.Uniqueifier.UNIQUE_LOCK);
        time5 = TimeInterceptor.currentTimeMillis();
        IntStream.range(0, 10).forEach(i -> Awaitility.await()
                .pollDelay(50L, TimeUnit.MILLISECONDS)
                .until(() -> {
                    assertThat(TimeInterceptor.currentTimeMillis() % 1000L).isZero();
                    return true;
                }));
        time6 = TimeInterceptor.currentTimeMillis();
        assertThat((time6 - time5)).isEqualTo(11_000L);
        TimeInterceptor.reset();
        assertThat(TimeInterceptor.currentInstant()).isNotEqualTo(time);
    }
}
