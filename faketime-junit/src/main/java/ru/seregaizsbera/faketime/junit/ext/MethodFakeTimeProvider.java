package ru.seregaizsbera.faketime.junit.ext;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import ru.seregaizsbera.faketime.junit.FakeTimeConfig;
import ru.seregaizsbera.faketime.junit.annotations.FakeTimeMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Реализация источника настроек времени, получаемых из метода, указанного в аннотации {@link FakeTimeMethod}
 */
public class MethodFakeTimeProvider extends AnnotationBasedFakeTimeProvider<FakeTimeMethod> {
    private static final String DEFAULT_GENERATION_METHOD_PREFIX = "fakeTime_";

    /**
     * Для модулей надо пустые конструкторы явно создавать
     */
    public MethodFakeTimeProvider() {
        // explicit default constructor
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Stream<FakeTimeConfig> getTime(ExtensionContext context, FakeTimeMethod annotation) {
        Class<?> testClass = context.getRequiredTestClass();
        Method testMethod = context.getRequiredTestMethod();
        String methodName = annotation.name();
        if (methodName == null || methodName.isEmpty()) {
            methodName = DEFAULT_GENERATION_METHOD_PREFIX + testMethod.getName();
        }
        try {
            Method provider = annotation.declaring().getMethod(methodName);
            if (!Modifier.isStatic(provider.getModifiers())) {
                throw new JUnitException(String.format(Locale.ROOT, "Метод %s.%s() должен быть static (%s)", testClass.getSimpleName(), methodName, "ghud"));
            }
            Object result = provider.invoke(null);
            if (result.getClass().isArray()) {
                FakeTimeConfig[] array = (FakeTimeConfig[]) result;
                return Arrays.stream(array);
            }
            if (result instanceof Stream<?> stream) {
                return (Stream<FakeTimeConfig>) stream;
            }
            if (result instanceof FakeTimeConfig config) {
                return Stream.of(config);
            }
            if (result instanceof Iterable<?> iterable) {
                return (Stream<FakeTimeConfig>) StreamSupport.stream(iterable.spliterator(), false);
            }
            throw new JUnitException(String.format(Locale.ROOT, "Метод %s.%s() возвращает неожиданный тип %s (%s)", testClass.getSimpleName(), methodName, result.getClass().getName(), "oipl"));
        } catch (NoSuchMethodException e) {
            throw new JUnitException(String.format(Locale.ROOT, "Метод %s() не найден в классе %s (%s)", methodName, annotation.declaring().getSimpleName(), "kwwc"));
        } catch (ReflectiveOperationException e) {
            throw new JUnitException(Optional.of(e)
                    .map(Throwable::getCause)
                    .map(Throwable::toString).
                    orElse(e.toString()));
        }
    }
}
