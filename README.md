<!-- TOC -->
* [Модуль управления системным временем](#модуль-управления-системным-временем)
  * [FakeTime API](#faketime-api)
  * [Аннотации JUnit5](#аннотации-junit5)
  * [Подробное описание](#подробное-описание)
  * [Ограничения и проблемы](#ограничения-и-проблемы)
<!-- TOC -->

# Модуль управления системным временем

Проект faketime содержит 2 библиотеки, позволяющих произвольно устанавливать в JVM системное время.

1. ru.seregaizsbera.faketime:faketime - содержит интерфейс API для управления временем и его реализацию.
2. ru.seregaizsbera.faketime:faketime-junit - содержит аннотации для управления временем в теста JUnit5.

## FakeTime API

Подключите библиотеку
```groovy
dependencies {
    implementation("ru.seregaizsbera.faketime:faketime")
}
```

Используйте объект класса `FakeTime`.
```java
FakeTime fakeTime = FakeTime.install();
fakeTime.shift(50L,TimeUnit.SECONDS);
```

К параметрам JVM добавьте опции:
1. `-XX:+EnableDynamicAgentLoading`
2. `--add-exports java.base/jdk.internal.misc=ALL-UNNAMED`
3. `--add-reads ru.seregaizsbera.faketime=ALL-UNNAMED` - если приложение использует модули

Подробности об имеющихся в API функциях можно прочитать документацию JavaDoc.

## Аннотации JUnit5

Подключите библиотеку
```groovy
dependencies {
    testImplementation("ru.seregaizsbera.faketime:faketime-junit")
}

tasks.withType(Test).configureEach {
    jvmArgs += ["-XX:+EnableDynamicAgentLoading"]
    jvmArgs += ["--add-exports", "java.base/jdk.internal.misc=ALL-UNNAMED"]
}
```

Добавьте к тесту аннотацию `@FakeTime`
```java
@Test
@FakeTime(dateTime = "2019-02-22T04:00:34.123+03:00")
void test() {
    // мы в 2019-м году
}
```

Можно указать несколько аннотаций, но тогда надо будет добавить аннотацию `@FakeTimeTest` вместо `@Test`.
```java
@FakeTimeTest
@FakeTime(dateTime = "2012-12-12T23:59:57.123Z",           name = "Возле полуночи UTC",     mode = RESET)
@FakeTime(dateTime = "2012-12-12T23:59:57.123+03:00",      name = "Возле полуночи MSK",     mode = RESET)
@FakeTime(dateTime = "2012-12-12T22:30:55.123Z",           name = "Конец суток UTC",        mode = RESET)
@FakeTime(dateTime = "2012-12-12T01:30:55.123Z",           name = "Начало суток UTC",       mode = RESET)
@FakeTime(dateTime = "2012-12-12T22:30:55.123+03:00",      name = "Конец суток MSK",        mode = RESET)
@FakeTime(dateTime = "2012-12-12T01:30:55.123+03:00",      name = "Начало суток MSK",       mode = RESET)
@FakeTime(tick = 10L, tickUnit = MILLISECONDS,             name = "По 10 миллисекунд",      mode = SHIFTED)
@FakeTime(            tickUnit = SECONDS,                  name = "По 1 секунде",           mode = SHIFTED)
@FakeTime(uniqueifier = UNIQUE_ATOMIC, tickUnit = SECONDS, name = "Уникально по 1 секунде", mode = SHIFTED)
@FakeTime(                                                 name = "Время остановлено",      mode = FIXED)
@FakeTime(timeZone = "UTC",                                name = "Таймзона UTC",           mode = SHIFTED)
@FakeTime(timeZone = "Europe/Moscow",                      name = "Таймзона Europe/Moscow", mode = SHIFTED)
@FakeTime(timeZone = "America/Ciudad_Juarez",              name = "Экзотическая таймзона",  mode = FIXED)
@FakeTimeMethod(declaring = Generator.class, name = "generate")
void test(FakeTimeConfig time) {
    // ...
}
```

```java
public class Generator {
    public static List<FakeTimeConfig> generate() {
        // ...
    }   
}
```

## Подробное описание

Подробное описание классов библиотеки находится в документации JavaDoc.

## Ограничения и проблемы

При инициализации библиотеки faketime в stdout выводится сообщение _Method [java.lang.System.currentTimeMillis()J] is annotated with @IntrinsicCandidate, but no compiler intrinsic is defined for the method_.
Это сообщение невозможно отключить. Его следует проигнорировать.

Библиотека реализована таким образом, что работает корректно только с временами в диапазоне ±148 лет от 01.01.1970. При необходимости
это ограничение можно устранить небольшой доработкой.

Поскольку системное время устанавливается глобально, запускать тесты можно только последовательно.
