package ru.seregaizsbera.faketime.junit.annotations;

import org.junit.jupiter.api.extension.ExtendWith;
import ru.seregaizsbera.faketime.junit.ext.ValueFakeTimeProvider;
import ru.seregaizsbera.faketime.junit.FakeTimeUniqueifier;
import ru.seregaizsbera.faketime.junit.FakeTimeMode;
import ru.seregaizsbera.faketime.junit.ext.FakeTimeTestExtension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Аннотация для тестовых методов, позволяющая задать различные режимы работы таймера
 * на время теста. Можно повесить на один метод несколько значений. Чтобы использовать несколько значений,
 * к методу следует добавить аннотацию {@link FakeTimeTest}.
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FakeTimes.class)
@Documented
@FakeTimeSpec(ValueFakeTimeProvider.class)
@ExtendWith(FakeTimeTestExtension.class)
public @interface FakeTime {

    /**
     * Режим установки часов
     */
    FakeTimeMode mode() default FakeTimeMode.IMPLICIT;

    /**
     * Сдвиг часов относительно базового таймера
     */
    long shift() default 0L;

    /**
     * Единицы измерения {@link #shift()}
     */
    TimeUnit shiftUnit() default TimeUnit.NANOSECONDS;

    /**
     * Время, на которое установлен базовый таймер, относительно Unix Epoch
     */
    long epoch() default 0L;

    /**
     * Единицы измерения {@link #epoch()}
     */
    TimeUnit epochUnit() default TimeUnit.NANOSECONDS;

    /**
     * Время, на которое установлен базовый таймер. Имеет приоритет над {@link #epoch()}
     */
    String dateTime() default "";

    /**
     * Формат для {@link #dateTime()}
     */
    String dateTimeFormat() default "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    /**
     * Размер такта таймера. Все значения времени, возвращаемые системными функциями, будут кратны
     * данному значению.
     */
    long tick() default 1L;

    /**
     * Единицы измерения для {@link #tick}
     */
    TimeUnit tickUnit() default TimeUnit.NANOSECONDS;

    /**
     * Таймзона
     */
    String timeZone() default "";

    /**
     * Имя конфигурации. Используется для формирования имени теста.
     */
    String name() default "";

    /**
     * Изменение скорости таймера
     */
    Accelerator accelerator() default @Accelerator;

    /**
     * Вариант обеспечения уникальности значений
     */
    FakeTimeUniqueifier uniqueifier() default FakeTimeUniqueifier.NOTHING;

    /**
     * Изменение скорости таймера. Стандартная скорость умножается на числитель и делится на знаменатель.
     */
    @interface Accelerator {
        /**
         * Числитель
         */
        long numerator() default 1L;

        /**
         * Знаменатель
         */
        long denominator() default 1L;
    }
}
