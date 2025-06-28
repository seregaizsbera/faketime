package ru.seregaizsbera.faketime.junit;

/**
 * Изменение скорости таймера относительно стандартной. Стандартная скорость умножается на числитель
 * и делится на знаменатель.
 * @param numerator числитель
 * @param denominator знаменатель
 */
public record Accelerator(long numerator, long denominator) {
}
