package ru.mirea.bakery.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Чтение и запись CSV-файлов с разделителем «;» в кодировке UTF-8 (с BOM, как в Python-версии). */
public final class CsvTable {
    public static final char DELIMITER = ';';
    private static final String BOM = "﻿";

    private CsvTable() { }

    /** Читает файл и возвращает строки как словари «столбец - значение». */
    public static List<Map<String, String>> read(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<Map<String, String>> rows = new ArrayList<>();
        if (lines.isEmpty()) {
            return rows;
        }
        String[] header = lines.getFirst().replace(BOM, "").split(String.valueOf(DELIMITER), -1);
        for (String line : lines.subList(1, lines.size())) {
            if (line.isBlank()) {
                continue;
            }
            String[] cells = line.split(String.valueOf(DELIMITER), -1);
            Map<String, String> row = new HashMap<>();
            for (int i = 0; i < header.length; i++) {
                row.put(header[i], i < cells.length ? cells[i] : "");
            }
            rows.add(row);
        }
        return rows;
    }

    /** Записывает строки в файл; порядок столбцов задаёт fields. Строки разделяются CRLF. */
    public static void write(Path path, List<String> fields, List<LinkedHashMap<String, String>> rows)
            throws IOException {
        StringBuilder sb = new StringBuilder(BOM);
        sb.append(join(fields)).append("\r\n");
        for (Map<String, String> row : rows) {
            List<String> cells = new ArrayList<>();
            for (String f : fields) {
                cells.add(row.getOrDefault(f, ""));
            }
            sb.append(join(cells)).append("\r\n");
        }
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
    }

    private static String join(List<String> cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                sb.append(DELIMITER);
            }
            sb.append(quote(cells.get(i)));
        }
        return sb.toString();
    }

    private static String quote(String cell) {
        if (cell.indexOf(DELIMITER) >= 0 || cell.indexOf('"') >= 0 || cell.indexOf('\n') >= 0
                || cell.indexOf('\r') >= 0) {
            return '"' + cell.replace("\"", "\"\"") + '"';
        }
        return cell;
    }
}
