package ru.seregaizsbera.faketime.junit.impl;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import java.util.List;
import java.util.Locale;

/**
 * Контекст теста, вызванного по шаблону. Создает имя, основанное на контексте и конфигурации.
 * @param extension расширение, содержащее конфигурациюю
 * @param displayName имя теста из тестового контекста
 * @param methodName имя тестового метода
 */
public record FakeTimeTestTemplateInvocationContext(FakeTimeConfigExtension extension, String displayName, String methodName) implements TestTemplateInvocationContext {

    @Override
    public String getDisplayName(int invocationIndex) {
        String name = extension.timeConfig().name();
        if (name == null || name.isEmpty()) {
            String base;
            if (displayName == null || displayName.isEmpty() || displayName.equals("()")) {
                base = methodName;
            } else {
                base = displayName;
            }
            return String.format(Locale.ROOT, "%s{%d}", base, invocationIndex);
        } else {
            return String.format(Locale.ROOT, "%s{%s}", methodName, name);
        }
    }

    @Override
    public List<Extension> getAdditionalExtensions() {
        return List.of(extension);
    }
}
