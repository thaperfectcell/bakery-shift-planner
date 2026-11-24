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

    /**
     * Читает файл и возвращает строки как словари «столбец - значение». Поддерживаются
     * значения в кавычках с разделителем, удвоенными кавычками и переводами строк внутри,
     * как в модуле csv Python и в методе {@link #write}. Пустые строки пропускаются.
     */
    public static List<Map<String, String>> read(Path path) throws IOException {
        String text = Files.readString(path, StandardCharsets.UTF_8);
        if (text.startsWith(BOM)) {
            text = text.substring(BOM.length());
        }
        List<List<String>> records = parse(text);
        List<Map<String, String>> rows = new ArrayList<>();
        if (records.isEmpty()) {
            return rows;
        }
        List<String> header = records.getFirst();
        for (List<String> cells : records.subList(1, records.size())) {
            Map<String, String> row = new HashMap<>();
            for (int i = 0; i < header.size(); i++) {
                row.put(header.get(i), i < cells.size() ? cells.get(i) : "");
            }
            rows.add(row);
        }
        return rows;
    }

    /** Разбирает текст CSV на записи и поля (конечный автомат по символам). */
    static List<List<String>> parse(String text) {
        List<List<String>> records = new ArrayList<>();
        List<String> record = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean fieldStarted = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < text.length() && text.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else if (c == '"') {
                    quoted = false;
                } else {
                    field.append(c);
                }
            } else if (c == '"' && field.isEmpty()) {
                quoted = true;
                fieldStarted = true;
            } else if (c == DELIMITER) {
                record.add(field.toString());
                field.setLength(0);
                fieldStarted = true;
            } else if (c == '\r' || c == '\n') {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
                endRecord(records, record, field, fieldStarted);
                record = new ArrayList<>();
                fieldStarted = false;
            } else {
                field.append(c);
                fieldStarted = true;
            }
        }
        endRecord(records, record, field, fieldStarted);
        return records;
    }

    private static void endRecord(List<List<String>> records, List<String> record, StringBuilder field,
                                  boolean fieldStarted) {
        if (fieldStarted || !record.isEmpty()) {
            record.add(field.toString());
            records.add(record);
        }
        field.setLength(0);
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
