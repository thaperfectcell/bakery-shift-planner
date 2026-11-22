package ru.mirea.bakery.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Вывод ООП-версии на демонстрационных данных совпадает с выводом процедурной версии (КТ2). */
class SameResultAsPythonTest {

    @Test
    void consoleOutputMatchesPythonVersion(@TempDir Path out) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            assertEquals(0, Main.run(new String[] {"--data", "data", "--out", out.toString()}));
        } finally {
            System.setOut(original);
        }
        String actual = buffer.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        String expected = Files.readString(Path.of("src/test/resources/expected_output.txt"), StandardCharsets.UTF_8)
                .replace("\r\n", "\n");
        String actualWithoutFooter = actual.substring(0, actual.lastIndexOf("\n\nРезультаты сохранены")) + "\n";
        assertEquals(expected, actualWithoutFooter);
        try (var files = Files.list(out)) {
            assertEquals(6, files.count());
        }
    }
}
