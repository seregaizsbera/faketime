package ru.seregaizsbera.faketime.junit.ext;

import org.junit.jupiter.api.extension.ExtensionContext;
import ru.seregaizsbera.faketime.junit.FakeTimeConfig;
import ru.seregaizsbera.faketime.junit.FakeTimeProvider;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Добавляет к стандартному методу {@link FakeTimeProvider#getTimeConfigs(ExtensionContext)} информацию об аннотации.
 * Реализации источников настроек времени могут наследовать данный класс, чтобы использовать аннотацию.
 * @param <A> тип аннотации
 */
public abstract class AnnotationBasedFakeTimeProvider<A extends Annotation> implements FakeTimeProvider, Consumer<Annotation> {
    private A annotation;

    /**
     * Конструктор по умолчанию
     */
    protected AnnotationBasedFakeTimeProvider() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void accept(Annotation annotation) {
        this.annotation = (A) annotation;
    }

    @Override
    public Stream<FakeTimeConfig> getTimeConfigs(ExtensionContext extensionContext) {
        return getTime(extensionContext, annotation);
    }

    /**
     * Возвращает настройки времени на основании аннотации.
     *
     * @param extensionContext контекст теста
     * @param annotation аннотация
     * @return настройки времени
     */
    protected abstract Stream<FakeTimeConfig> getTime(ExtensionContext extensionContext, A annotation);
}
