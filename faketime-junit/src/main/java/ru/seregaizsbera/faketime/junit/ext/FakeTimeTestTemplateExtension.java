package ru.seregaizsbera.faketime.junit.ext;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.JUnitException;
import ru.seregaizsbera.faketime.junit.FakeTimeConfig;
import ru.seregaizsbera.faketime.junit.FakeTimeProvider;
import ru.seregaizsbera.faketime.junit.annotations.FakeTime;
import ru.seregaizsbera.faketime.junit.annotations.FakeTimeMethod;
import ru.seregaizsbera.faketime.junit.annotations.FakeTimeSpec;
import ru.seregaizsbera.faketime.junit.impl.FakeTimeConfigExtension;
import ru.seregaizsbera.faketime.junit.impl.FakeTimeTestTemplateInvocationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Тестовое расширение JUnit. Создает отдельные экземпляры тестов по шаблону и настройкам времени.
 */
public class FakeTimeTestTemplateExtension implements TestTemplateInvocationContextProvider {
    /**
     * Для модулей надо пустые конструкторы явно создавать
     */
    public FakeTimeTestTemplateExtension() {
        // explicit default constructor
    }

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getTestMethod()
                .stream()
                .map(AccessibleObject::getAnnotations)
                .flatMap(Arrays::stream)
                .map(Annotation::annotationType)
                .map(a -> a.getAnnotation(ExtendWith.class))
                .filter(Objects::nonNull)
                .map(ExtendWith::value)
                .flatMap(Arrays::stream)
                .anyMatch(c -> c.equals(FakeTimeTestTemplateExtension.class));
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        return extractConfig(context)
                .map(FakeTimeConfigExtension::new)
                .map(it -> new FakeTimeTestTemplateInvocationContext(it, context.getDisplayName(), context.getRequiredTestMethod().getName()));
    }

    private Stream<FakeTimeConfig> extractConfig(ExtensionContext extensionContext) {
        return extensionContext.getTestMethod()
                .stream()
                .flatMap(method -> Stream.of(FakeTime.class, FakeTimeMethod.class)
                        .map(method::getAnnotationsByType)
                        .flatMap(Arrays::stream))
                .flatMap(annotation -> {
                    Class<? extends FakeTimeProvider> providerClass = annotation.annotationType()
                            .getAnnotation(FakeTimeSpec.class)
                            .value();
                    try {
                        var constructor = providerClass.getConstructor();
                        FakeTimeProvider provider = constructor.newInstance();
                        if (provider instanceof AnnotationBasedFakeTimeProvider<?> annotationBasedFakeTimeProvider) {
                            annotationBasedFakeTimeProvider.accept(annotation);
                            return annotationBasedFakeTimeProvider.getTimeConfigs(extensionContext);
                        } else {
                            return provider.getTimeConfigs(extensionContext);
                        }
                    } catch (ReflectiveOperationException e) {
                        throw new JUnitException(String.format(Locale.ROOT, "%s (%s)", e.getMessage(), "spat"));
                    }
                });
    }
}
