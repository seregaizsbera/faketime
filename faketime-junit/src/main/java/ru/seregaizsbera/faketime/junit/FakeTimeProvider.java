package ru.seregaizsbera.faketime.junit;

import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.stream.Stream;

/**
 * Предоставляет системе запуска тестов данные о настройках времени.
 */
public interface FakeTimeProvider {
    /**
     * Возвращает набор настроек времени
     * @param extensionContext тестовый контекст
     * @return набор настроек времени
     */
    Stream<FakeTimeConfig> getTimeConfigs(ExtensionContext extensionContext);
}
