package ru.seregaizsbera.faketime;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class FakeTimeTest {

    @Test
    void testTime() {
        Random random = new Random();
        FakeTime fakeTime = FakeTime.install();
        assertThat(fakeTime).isNotNull();
        try {
            Instant instant = OffsetDateTime.of(2025, 4, 16, 13, 35, 45, 678_000_000, ZoneOffset.UTC).toInstant();
            fakeTime.reset();
            fakeTime.fix();
            fakeTime.fixAt(random.nextLong());
            fakeTime.fixAt(instant);
            fakeTime.fixAt(random.nextLong(), random.nextInt(0, 999_999_999));
            fakeTime.shift(random.nextLong());
            fakeTime.shift(Duration.of(random.nextInt(), TimeUnit.MILLISECONDS.toChronoUnit()));
            fakeTime.shift(random.nextLong(), TimeUnit.MILLISECONDS);
            fakeTime.resetAt(random.nextLong());
            fakeTime.resetAt(instant);
            fakeTime.resetAt(random.nextLong(), random.nextInt(0, 999_999_999));
            fakeTime.setTick(1L);
            fakeTime.setTick(Duration.of(1L, TimeUnit.SECONDS.toChronoUnit()));
            fakeTime.resetTick();
            fakeTime.setUniqueifier(FakeTimeUniqueifier.UNIQUE_ATOMIC);
            fakeTime.resetUniqueifier();
            fakeTime.fix();
            fakeTime.resetAccelerator();
            fakeTime.fix();
            Instant t1 = Instant.now();
            Awaitility.await().pollDelay(50, TimeUnit.MILLISECONDS).until(() -> true);
            Instant t2 = Instant.now();
            assertThat(t1).isEqualTo(t2);
        } finally {
            fakeTime.reset();
        }
    }
}
