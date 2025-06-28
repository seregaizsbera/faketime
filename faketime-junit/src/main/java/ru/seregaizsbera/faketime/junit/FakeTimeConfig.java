package ru.seregaizsbera.faketime.junit;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Содержит настройки времени, получаемые из аннотаций
 */
public interface FakeTimeConfig {
    /**
     * Режим установки часов
     */
    FakeTimeConfigMode mode();

    /**
     * Сдвиг часов относительно базового таймера.
     */
    long shift();

    /**
     * Единицы измерения {@link #shift()}
     */
    TimeUnit shiftUnit();

    /**
     * Время, на которое установлен базовый таймер, относительно Unix Epoch
     */
    long epoch();

    /**
     * Единицы измерения {@link #epoch()}
     */
    TimeUnit epochUnit();

    /**
     * Размер такта таймера. Все значения времени, возвращаемые системными функциями, будут кратны
     * данному значению.
     */
    long tick();

    /**
     * Единицы измерения для {@link #tick}
     */
    TimeUnit tickUnit();

    /**
     * Таймзона
     */
    String timeZone();

    /**
     * Изменение скорости таймера относительно стандартной
     */
    Accelerator accelerator();

    /**
     * Вариант обеспечения уникальности значений
     */
    FakeTimeUniqueifier uniqueifier();

    /**
     * Имя конфигурации. Используется для формирования имени теста.
     */
    String name();

    /**
     * Инструментальный класс для создания экземпляров объектов {@link FakeTimeConfig}
     */
    class Builder {
        private FakeTimeConfigMode mode = FakeTimeConfigMode.RESET;
        private long shift = 0L;
        private TimeUnit shiftUnit = TimeUnit.NANOSECONDS;
        private long epoch = Long.MIN_VALUE;
        private TimeUnit epochUnit = TimeUnit.NANOSECONDS;
        private long tick = 1L;
        private TimeUnit tickUnit = TimeUnit.NANOSECONDS;
        private String timeZone = "";
        private long numerator = 1L;
        private long denominator = 1L;
        private FakeTimeUniqueifier uniqueifier = FakeTimeUniqueifier.NOTHING;
        private final String name;

        private Builder(String name) {
            this.name = name;
        }

        /**
         * Выбрать режим установки времени
         * @param mode режим установки времени
         * @return этот объект
         */
        public Builder mode(FakeTimeConfigMode mode) {
            this.mode = mode;
            return this;
        }

        /**
         * Установить сдвиг часов относительно базового таймера
         * @param shift сдвиг часов относительно базового таймера
         * @param shiftUnit единицы измерения
         * @return этот объект
         */
        @SuppressWarnings("UnusedReturnValue")
        public Builder shift(long shift, TimeUnit shiftUnit) {
            this.shift = shift;
            this.shiftUnit = shiftUnit;
            return this;
        }

        /**
         * Установить базовый таймер на указанное время относительно Unix Epoch
         * @param epoch новое время
         * @param epochUnit единицы измерения
         * @return этот объект
         */
        @SuppressWarnings("UnusedReturnValue")
        public Builder epoch(long epoch, TimeUnit epochUnit) {
            this.epoch = epoch;
            this.epochUnit = epochUnit;
            return this;
        }

        /**
         * Установить базовый таймер на указанное время
         * @param year год
         * @param month месяц
         * @param dayOfMonth день
         * @param hour часы
         * @param minute минуты
         * @param second секунды
         * @param nanoOfSecond наносекунды
         * @param timeZone таймзона
         * @return этот объект
         */
        @SuppressWarnings("java:S107")
        public Builder instant(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond, String timeZone) {
            var instant = ZonedDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, ZoneId.of(timeZone)).toInstant();
            this.epoch = instant.getEpochSecond() * 1_000_000_000L + instant.getNano();
            this.epochUnit = TimeUnit.NANOSECONDS;
            return this;
        }

        /**
         * Установить размер такта часов
         * @param tick размер такта
         * @param tickUnit единицы измерения
         * @return этот объект
         */
        public Builder tick(long tick, TimeUnit tickUnit) {
            this.tick = tick;
            this.tickUnit = tickUnit;
            return this;
        }

        /**
         * Установить таймзону
         * @param timeZone таймзона
         * @return этот объект
         */
        public Builder timeZone(String timeZone) {
            this.timeZone = timeZone;
            return this;
        }

        /**
         * Установить изменение скорости таймера относительно стандартной
         * @param numerator числитель
         * @param denominator знаменатель
         * @return этот объект
         */
        public Builder accelerate(long numerator, long denominator) {
            this.numerator = numerator;
            this.denominator = denominator;
            return this;
        }

        /**
         * Установить вариант обеспечения уникальности
         * @param uniqueifier вариант обеспечения уникальности
         * @return этот объект
         */
        public Builder uniqueifier(FakeTimeUniqueifier uniqueifier) {
            this.uniqueifier = uniqueifier;
            return this;
        }

        /**
         * Создает новую конфигурацию на основании сохраненных параметров
         * @return новая конфигурация
         */
        public FakeTimeConfig build() {
            return new Value(mode, shift, shiftUnit, epoch, epochUnit, tick, tickUnit, timeZone,
                    new Accelerator(numerator, denominator), uniqueifier, name);
        }

        private record Value(FakeTimeConfigMode mode,
                             long shift, TimeUnit shiftUnit,
                             long epoch, TimeUnit epochUnit,
                             long tick, TimeUnit tickUnit,
                             String timeZone,
                             Accelerator accelerator,
                             FakeTimeUniqueifier uniqueifier,
                             String name) implements FakeTimeConfig {
        }
    }

    /**
     * Создает построитель конфигурации
     * @return новый построитель конфигурации
     */
    static Builder builder() {
        return new Builder("");
    }

    /**
     * Создает построитель конфигурации
     *
     * @param name имя конфигурации
     * @return новый построитель конфигурации
     */
    static Builder builder(String name) {
        return new Builder(name);
    }
}
