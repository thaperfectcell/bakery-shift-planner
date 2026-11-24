package ru.mirea.bakery.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Вывод ООП-версии на демонстрационных данных совпадает с выводом процедурной версии (КТ2):
 * консольный отчёт и все шесть файлов CSV. Эталоны получены командой
 * {@code python main.py --data data --out <каталог>} и лежат в src/test/resources.
 */
class SameResultAsPythonTest {
    private static final Path EXPECTED = Path.of("src/test/resources/expected");
    private static final List<String> RESULT_FILES = List.of("plan.csv", "material_requirements.csv",
            "shortages.csv", "batches.csv", "shift_tasks.csv", "requisition.csv");

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
            assertEquals(RESULT_FILES.size(), files.count());
        }
        for (String name : RESULT_FILES) {
            assertArrayEquals(Files.readAllBytes(EXPECTED.resolve(name)), Files.readAllBytes(out.resolve(name)),
                    "файл " + name + " отличается от результата Python-версии");
        }
    }
}
