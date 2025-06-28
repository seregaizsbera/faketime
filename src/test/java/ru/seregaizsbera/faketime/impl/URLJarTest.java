package ru.seregaizsbera.faketime.impl;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class URLJarTest {
    @Test
    void testUrlJar() throws IOException, URISyntaxException {
        var file3 = prepareJar();
        try (
                var cl = new URLClassLoader(new URL[]{file3.toUri().toURL()});
                var jar = URLJarFile.getJarFile(cl.getResource("interceptors1.jar"))
        ) {
            assertThat(jar.entries()).isNotNull();
        } finally {
            try { Files.deleteIfExists(file3); } catch (IOException ignored) {}
        }
        file3 = prepareJar();
        try (var cl = new URLClassLoader(new URL[]{file3.toUri().toURL()})) {
            var url = cl.getResource("interceptors1.jar");
            assertThat(url).isNotNull();
            var url2 = new URI(url.toString().replace("interceptors1.jar", "interceptors2.jar")).toURL();
            assertThatThrownBy(() -> URLJarFile.getJarFile(url2)).isInstanceOf(UncheckedIOException.class);
        } finally {
            Files.delete(file3);
        }
    }

    private Path prepareJar() throws IOException {
        var file1 = Files.createTempFile("test-url-jar-", ".txt");
        var file2 = Files.createTempFile("test-url-jar-", ".jar");
        var file3 = Files.createTempFile("test-url-jar-", ".jar");
        try (var out = Files.newBufferedWriter(file1)) {
            out.append("Hello, World!");
            out.newLine();
        }
        try (
                var out1 = Files.newOutputStream(file2);
                var out2 = new ZipOutputStream(out1)
        ) {
            out2.putNextEntry(new ZipEntry("hello.txt"));
            Files.copy(file1, out2);
            out2.closeEntry();
        }
        try (
                var out1 = Files.newOutputStream(file3);
                var out2 = new ZipOutputStream(out1)
        ) {
            out2.putNextEntry(new ZipEntry("interceptors1.jar"));
            Files.copy(file2, out2);
            out2.closeEntry();
        }
        Files.delete(file1);
        Files.delete(file2);
        return file3;
    }
}
