package ru.seregaizsbera.faketime.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

/**
 *  Реализация загрузки jar-файла по URL. На основе {@code sun.net.www.protocol.jar.URLJarFile}.
 */
class URLJarFile extends JarFile {
    private final Path file;

    static URLJarFile getJarFile(URL url) {
        try {
            return retrieve(url);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static URLJarFile retrieve(URL url) throws IOException {
        Path tmpFile = Files.createTempFile("faketime-", ".jar");
        try (InputStream in = url.openConnection().getInputStream()) {
            Files.copy(in, tmpFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            delete(tmpFile);
            throw e;
        }
        return new URLJarFile(tmpFile);
    }

    private URLJarFile(Path file) throws IOException {
        super(file.toFile(), false, ZipFile.OPEN_READ);
        this.file = file;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            delete(file);
        }
    }

    private static void delete(Path file) {
        try {
            Files.delete(file);
        } catch (IOException ignored) {
            // ignore
        }
    }
}
