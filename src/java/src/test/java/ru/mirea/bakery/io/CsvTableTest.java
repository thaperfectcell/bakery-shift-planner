package ru.mirea.bakery.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Чтение CSV со значениями в кавычках (замечание рецензента #1). */
class CsvTableTest {

    @Test
    void delimiterInsideQuotesIsPartOfValue(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("products.csv");
        Files.writeString(file, "name;unit\r\n\"Хлеб; свежий\";шт\r\n", StandardCharsets.UTF_8);

        List<Map<String, String>> rows = CsvTable.read(file);

        assertEquals(1, rows.size());
        assertEquals("Хлеб; свежий", rows.getFirst().get("name"));
        assertEquals("шт", rows.getFirst().get("unit"));
    }

    @Test
    void writtenFileIsReadBackUnchanged(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("out.csv");
        LinkedHashMap<String, String> row = new LinkedHashMap<>();
        row.put("id", "P01");
        row.put("name", "Батон «Нарезной»; в упаковке");
        row.put("note", "сказал \"срочно\"\nвторая строка");
        row.put("empty", "");
        CsvTable.write(file, List.of("id", "name", "note", "empty"), List.of(row));

        List<Map<String, String>> rows = CsvTable.read(file);

        assertEquals(List.of(Map.copyOf(row)), rows);
    }

    @Test
    void blankLinesAndMissingCellsAreHandledLikePythonCsv() {
        assertEquals(List.of(List.of("a", "b"), List.of("1", "")), CsvTable.parse("a;b\n\n1;\n"));
        assertEquals(List.of(List.of("a"), List.of("x\"y")), CsvTable.parse("a\r\n\"x\"\"y\""));
    }
}
