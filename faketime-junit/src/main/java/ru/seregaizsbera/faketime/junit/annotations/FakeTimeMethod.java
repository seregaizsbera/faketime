package ru.seregaizsbera.faketime.junit.annotations;

import ru.seregaizsbera.faketime.junit.ext.MethodFakeTimeProvider;
import ru.seregaizsbera.faketime.junit.FakeTimeConfig;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для тестовых методов, отмеченных аннотацией {@link FakeTimeTest}. Задает
 * имя метода в тестовом классе, который будет использован в качестве источника для настроек времени.
 * Метод должен быть статическим и возвращать коллекцию {@link FakeTimeConfig}.
 * В качестве коллекций допускаются {@linkplain java.util.stream.Stream}, {@linkplain Iterable}, массив
 * или одиночный экземпляр.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(FakeTimeMethods.class)
@FakeTimeSpec(MethodFakeTimeProvider.class)
public @interface FakeTimeMethod {
    /**
     * Класс, в котором находится метод.
     */
    Class<?> declaring() default void.class;

    /**
     * Имя метода, возвращающего объекты {@link FakeTimeConfig} в одном экземпляре или
     * в составе {@linkplain java.util.stream.Stream}, {@linkplain Iterable} или массива. По умолчанию
     * используется метод с именем {@code fakeTime_имяТестовогоМетода}. Метод должен быть публичным и статическим.
     */
    String name() default "";
}
