package ru.seregaizsbera.faketime.internal;

import jdk.internal.misc.VM;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

/**
 * Данный класс содержит реализации системных методов, которые будут подставлены агентом
 * вместо оригинальных. Эти реализации нужны для тестов, которым требуются особые значения
 * времени. Порядок вызова функций имеет значение.
 * <ol>
 *     <li>{@link #fix()}, {@link #reset()}</li>
 *     <li>
 *         {@link #fixAt(long)},
 *         {@link #shift(long)},
 *         {@link #resetAt(long)},
 *     </li>
 *     <li>{@link #setTick(long)}</li>
 *     <li>{@link #setUniqueifier(Uniqueifier)}</li>
 * </ol>
 * Функции из каждой группы сбрасывают значения, определяемые функциями из нижних групп, на значения по умолчанию.
 */
public final class TimeInterceptor {
    private static final long NANOS_PER_SEC = 1_000_000_000L;
    private static final long NANOS_PER_MS = 1_000_000L;
    private static final LongUnaryOperator ZERO = l -> 0L;
    @SuppressWarnings("java:S3077")
    private static volatile State state = new State(VM::getNanoTimeAdjustment, 0L, 1L, Accelerator.UNIT, Uniqueifier.NOTHING.operator);
    private TimeInterceptor() {}

    /**
     * Сбросить вск настройки таймера к системным значениям по умолчанию
     */
    public static void reset() {
        state = new State(VM::getNanoTimeAdjustment, 0L, 1L, Accelerator.UNIT, Uniqueifier.NOTHING.operator);
    }

    /**
     * Остановить таймер на текущем системном времени
     */
    public static void fix() {
        state = new State(ZERO, VM.getNanoTimeAdjustment(0L), 1L, Accelerator.UNIT, Uniqueifier.NOTHING.operator);
    }

    /**
     * Установить время системного таймера на указанный момент и остановить его.
     * Все обращения к таймеру после вызова этого метода будут возвращать одно и то же значение.
     *
     * @param epochNs количество наносекунд после Unix Epoch
     */
    public static void fixAt(long epochNs) {
        state = new State(ZERO, epochNs, 1L, Accelerator.UNIT, Uniqueifier.NOTHING.operator);
    }

    /**
     * Сдвинуть время на указанный интервал относительно системного таймера
     *
     * @param shiftNs размер сдвига в наносекундах
     */
    public static void shift(long shiftNs) {
        state = new State(VM::getNanoTimeAdjustment, shiftNs, 1L, Accelerator.UNIT, Uniqueifier.NOTHING.operator);
    }

    /**
     * Сдвинуть время системного таймера на указанный момент
     *
     * @param epochNs количество наносекунд после Unix Epoch
     */
    public static void resetAt(long epochNs) {
        state = new State(VM::getNanoTimeAdjustment, epochNs - VM.getNanoTimeAdjustment(0L), 1L, Accelerator.UNIT, Uniqueifier.NOTHING.operator);
    }

    /**
     * Изменить скорость таймера. Скорость таймера умножается на числитель и делится на знаменатель.
     * Если числитель или знаменатель меньше или равен нулю, то ничего не делает.
     *
     * @param numerator числитель
     * @param denominator знаменатель
     */
    public static void setAccelerator(long numerator, long denominator) {
        if (numerator <= 0L || denominator <= 0L) {
            return;
        }
        long gcd = computeGCD(numerator, denominator);
        numerator /= gcd;
        denominator /= gcd;
        LongUnaryOperator accelerator;
        State current = state;
        if (numerator == denominator) {
            accelerator = Accelerator.UNIT;
        } else {
            accelerator= new Accelerator(current.counterNs.applyAsLong(0L), numerator, denominator);
        }
        state = new State(current.counterNs, current.shiftNs, current.tickNs, accelerator, current.uniqueifier);
    }

    /**
     * Наибольший общий делитель
     */
    private static long computeGCD(long x, long y) {
        while (y != 0L) {
            long temp = y;
            y = x % y;
            x = temp;
        }
        return x;
    }

    /**
     * Установить такт таймера. Все значения таймера будут кратны указанному такту.
     *
     * @param tickNs размер такта в наносекундах
     */
    public static void setTick(long tickNs) {
        if (tickNs < 0L) {
            tickNs = Math.abs(tickNs);
        }
        if (tickNs <= 0L) {
            tickNs = 1L;
        }
        State current = state;
        if (tickNs != current.tickNs) {
            state = new State(current.counterNs, current.shiftNs, tickNs, current.accelerator, Uniqueifier.NOTHING.operator);
        }
    }

    /**
     * Устанавливает функцию, обеспечивающую уникальность значений таймера. Все остальные функции
     * сбрасывают {@code uniqueifier} на пустую функцию.
     *
     * @param uniqueifier функция, модифицирующая результат
     */
    public static void setUniqueifier(Uniqueifier uniqueifier) {
        State current = state;
        if (uniqueifier.operator == current.uniqueifier) {
            return;
        }
        state = new State(current.counterNs, current.shiftNs, current.tickNs, current.accelerator, uniqueifier.operator);
    }

    /**
     * Реализация метода {@link System#currentTimeMillis()}
     *
     * @return значение таймера в соответствии с заданными настройками
     */
    @SuppressWarnings("unused")
    public static long currentTimeMillis() {
        return state.getEpochNs() / NANOS_PER_MS;
    }

    /**
     * Реализация метода {@link Clock#currentInstant()}
     *
     * @return значение таймера в соответствии с заданными настройками
     */
    @SuppressWarnings("unused")
    public static Instant currentInstant() {
        long epochNs = state.getEpochNs();
        return Instant.ofEpochSecond(epochNs / NANOS_PER_SEC, epochNs % NANOS_PER_SEC);
    }

    private record Accelerator(long zeroPointEpochNs, long numerator, long denominator) implements LongUnaryOperator {
        private static final LongUnaryOperator UNIT = LongUnaryOperator.identity();
        @Override
        public long applyAsLong(long epochNs) {
            return (epochNs - zeroPointEpochNs) * numerator / denominator + zeroPointEpochNs;
        }
    }

    /**
     * Состояние системного таймера. Собрано в отдельный неизменяемый объект для
     * обеспечения параллельного доступа.
     *
     * @param counterNs счетчик наносекунд
     * @param shiftNs сдвиг времени относительно счетчика
     * @param tickNs размер такта
     * @param uniqueifier функция, применяемая к результату после всех вычислений
     */
    private record State(LongUnaryOperator counterNs, long shiftNs, long tickNs, LongUnaryOperator accelerator, LongBinaryOperator uniqueifier) {
        public long getEpochNs() {
            long epochNs = accelerator.applyAsLong(counterNs.applyAsLong(0L)) + shiftNs;
            return uniqueifier.applyAsLong(epochNs - (epochNs % tickNs), tickNs);
        }
    }

    /**
     * Варианты реализации функции, обеспечивающей уникальность
     * значений, возвращаемых при каждом обращении к таймеру
     */
    public enum Uniqueifier {
        /**
         * Значения таймера выдаются без изменений
         */
        NOTHING((epochNs, tickNs) -> epochNs),
        /**
         * Значения таймера модифицируются в большую сторону для обеспечения уникальности.
         * Синхронизация параллельного доступа обеспечивается значением атомарной переменной.
         */
        UNIQUE_ATOMIC(AtomicUniqueifier.instance),
        /**
         * Значения таймера модифицируются в большую сторону для обеспечения уникальности.
         * Синхронизация параллельного доступа обеспечивается блокировкой.
         */
        UNIQUE_LOCK(LockUniqueifier.instance);

        private final LongBinaryOperator operator;

        Uniqueifier(LongBinaryOperator operator) {
            this.operator = operator;
        }
    }

    /**
     * Реализация механизма обеспечения уникальности для {@link Uniqueifier#UNIQUE_ATOMIC}.
     */
    private static final class AtomicUniqueifier implements LongBinaryOperator {
        private static final AtomicUniqueifier instance = new AtomicUniqueifier();
        private final AtomicLong lastResultNs = new AtomicLong(Long.MIN_VALUE);
        @Override
        public long applyAsLong(long epochNs, long tickNs) {
            while (true) {
                long lastNs = lastResultNs.get();
                if (epochNs <= lastNs) {
                    epochNs = lastNs + tickNs;
                }
                if (lastResultNs.compareAndSet(lastNs, epochNs)) {
                    return epochNs;
                }
            }
        }
    }

    /**
     * Реализация механизма обеспечения уникальности для {@link Uniqueifier#UNIQUE_LOCK}.
     */
    private static final class LockUniqueifier implements LongBinaryOperator {
        private static final LockUniqueifier instance = new LockUniqueifier();
        private final Lock lock = new ReentrantLock();
        private long lastResultNs = Long.MIN_VALUE;
        @Override
        public long applyAsLong(long epochNs, long tickNs) {
            lock.lock();
            try {
                long lastNs = lastResultNs;
                if (epochNs <= lastNs) {
                    epochNs = lastNs + tickNs;
                }
                lastResultNs = epochNs;
                return epochNs;
            } finally {
                lock.unlock();
            }
        }
    }
}
