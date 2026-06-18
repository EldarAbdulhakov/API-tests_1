package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileFactory {

    public static Path createFile(String prefix, String suffix, String content) {
        try {
            Path file = Files.createTempFile(prefix, suffix);
            Files.writeString(file, content);

            return file;

        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp file", e);
        }
    }
}
