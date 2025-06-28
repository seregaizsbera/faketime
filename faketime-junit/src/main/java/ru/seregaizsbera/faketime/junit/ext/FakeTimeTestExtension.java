package ru.seregaizsbera.faketime.junit.ext;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.JUnitException;
import ru.seregaizsbera.faketime.junit.FakeTimeConfig;
import ru.seregaizsbera.faketime.junit.annotations.FakeTime;
import ru.seregaizsbera.faketime.junit.impl.FakeTimeConfigExtension;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Тестовое расширение JUnit. Обрабатывает 1 вызов тестового метода с одиночной аннотацией {@link FakeTime}.
 */
public class FakeTimeTestExtension implements InvocationInterceptor {
    /**
     * Для модулей надо пустые конструкторы явно создавать
     */
    public FakeTimeTestExtension() {
        // explicit default constructor
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        Method testMethod = invocationContext.getExecutable();
        FakeTime[] annotations = testMethod.getAnnotationsByType(FakeTime.class);
        if (annotations.length > 1) {
            throw new JUnitException(String.format(Locale.ROOT, "На методе %s.%s() обнаружено больше 1 аннотации %s. Удалите лишние аннотации или используйте аннотацию %s (%s)",
                    testMethod.getDeclaringClass().getSimpleName(), testMethod.getName(), "FakeTime", "FakeTimeTest", "ixhd"));
        }
        if (annotations.length == 0) {
            throw new JUnitException(String.format(Locale.ROOT, "В методе %s.%s() возникла невозможная ситуация (%s)",
                    testMethod.getDeclaringClass().getSimpleName(), testMethod.getName(), "xrzg"));
        }
        FakeTimeConfig fakeTimeConfig = ValueFakeTimeProvider.makeFakeTimeConfig(annotations[0]);
        FakeTimeConfigExtension fakeTimeConfigExtension = new FakeTimeConfigExtension(fakeTimeConfig);
        fakeTimeConfigExtension.beforeEach(extensionContext);
        try {
            invocation.proceed();
        } finally {
            fakeTimeConfigExtension.afterEach(extensionContext);
        }
    }
}
