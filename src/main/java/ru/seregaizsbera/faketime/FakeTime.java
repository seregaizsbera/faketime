package ru.seregaizsbera.faketime;

import ru.seregaizsbera.faketime.impl.FakeTimeInternal;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Основной класс API, через который производится управление поведением системных функций, основанных на таймере.
 * Порядок вызова функций имеет значение.
 * <ol>
 *     <li>{@link #fix()}, {@link #reset()}</li>
 *     <li>
 *         {@link #fixAt(long)}, {@link #fixAt(Instant)}, {@link #fixAt(long, int)},
 *         {@link #shift(long)}, {@link #shift(long, TimeUnit)}, {@link #shift(Duration)},
 *         {@link #resetAt(long)}, {@link #resetAt(Instant)} , {@link #resetAt(long, int)},
 *     </li>
 *     <li>{@link #setTick(long)}, {@link #setTick(Duration)}, {@link #resetTick()}</li>
 *     <li>{@link #setAccelerator(long, long)}, {@link #resetAccelerator()}, {@link #setUniqueifier(FakeTimeUniqueifier)}</li>
 * </ol>
 * Функции из каждой группы сбрасывают значения, определяемые функциями из нижних групп, на значения по умолчанию.
 */
public interface FakeTime {
    /**
     * Установить агента и получить объект для управления временем
     *
     * @return объект для управления временем
     */
    static FakeTime install() {
        return FakeTimeInternal.get();
    }

    /**
     * Сбросить таймер и размер такта к системным значениям по умолчанию
     */
    void reset();

    /**
     * Остановить таймер на текущем системном времени
     */
    void fix();

    /**
     * Установить время системного таймера на указанный момент и оставноить его.
     * Все обращения к таймеру после вызова этого метода будут возвращать одно и то же значение.
     *
     * @param epochNs количество наносекунд после Unix Epoch
     */
    void fixAt(long epochNs);

    /**
     * Установить время системного таймера на указанный момент и оставноить его.
     * Все обращения к таймеру после вызова этого метода будут возвращать одно и то же значение.
     *
     * @param moment момент времени
     */
    void fixAt(Instant moment);

    /**
     * Установить время системного таймера на указанный момент и оставноить его.
     * Все обращения к таймеру после вызова этого метода будут возвращать одно и то же значение.
     *
     * @param epochSec количество секунд после Unix Epoch
     * @param nanoOfSecond количество наносекунд дополнительно к epochSec
     */
    void fixAt(long epochSec, int nanoOfSecond);

    /**
     * Сдвинуть время на указанный интервал относительно системного таймера
     *
     * @param shiftNs размер сдвига в наносекундах
     */
     void shift(long shiftNs);

    /**
     * Сдвинуть время на указанный интервал относительно системного таймера
     *
     * @param shift размер сдвига
     */
    void shift(Duration shift);

    /**
     * Сдвинуть время на указанный интервал относительно системного таймера
     *
     * @param shift размер сдвига
     * @param unit единицы измерения сдвига
     */
    void shift(long shift, TimeUnit unit);

    /**
     * Сдвинуть время системного таймера на указанный момент
     *
     * @param epochNs количество наносекунд после Unix Epoch
     */
    void resetAt(long epochNs);

    /**
     * Сдвинуть время системного таймера на указанный момент
     *
     * @param moment момент времени
     */
    void resetAt(Instant moment);

    /**
     * Сдвинуть время системного таймера на указанный момент
     *
     * @param epochSec количество секунд после Unix Epoch
     * @param nanoOfSecond количество наносекунд дополнительно к epochSec
     */
     void resetAt(long epochSec, int nanoOfSecond);

    /**
     * Установить такт таймера. Все значения таймера будут кратны указанному такту.
     *
     * @param tickNs размер такта в наносекундах
     */
    void setTick(long tickNs);

    /**
     * Установить такт таймера. Все значения таймера будут кратны указанному такту.
     *
     * @param tick размер такта
     */
    void setTick(Duration tick);

    /**
     * Сбросить размер такта до минимально возможного
     */
    void resetTick();

    /**
     * Изменить скорость таймера. Скорость таймера умножается на числитель и делится на знаменатель.
     * Если числитель или знаменатель меньше или равен нулю, то ничего не делает.
     *
     * @param numerator числитель
     * @param denominator знаменатель
     */
    void setAccelerator(long numerator, long denominator);

    /**
     * Установить стандартную скорость таймеру, эквивалентно вызову {@code setAccelerator(1, 1)}
     */
    void resetAccelerator();

    /**
     * Вариант обеспечения уникальности значений
     */
    void setUniqueifier(FakeTimeUniqueifier uniqueifier);

    /**
     * Отключает обеспечения уникальности значений
     */
    void resetUniqueifier();
}
