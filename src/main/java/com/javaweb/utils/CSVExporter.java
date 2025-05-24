package com.javaweb.utils;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Slf4j
public class CSVExporter {

    /**
     * Exports a JTable to a CSV file
     *
     * @param table The JTable to export
     * @param file  The File to save to
     * @throws IOException If there's an error writing to the file
     */
    public static void exportTableToCSV(JTable table, File file) throws IOException {
        TableModel model = table.getModel();
        FileWriter csv = new FileWriter(file);

        // Write headers
        for (int i = 0; i < model.getColumnCount(); i++) {
            csv.write(escapeCSV(model.getColumnName(i)));
            if (i < model.getColumnCount() - 1) {
                csv.write(",");
            }
        }
        csv.write("\n");

        // Write data rows
        for (int i = 0; i < model.getRowCount(); i++) {
            for (int j = 0; j < model.getColumnCount(); j++) {
                Object value = model.getValueAt(i, j);
                csv.write(escapeCSV(value != null ? value.toString() : ""));
                if (j < model.getColumnCount() - 1) {
                    csv.write(",");
                }
            }
            csv.write("\n");
        }

        csv.close();
        log.info("CSV file exported successfully to {}", file.getAbsolutePath());
    }

    /**
     * Escapes special characters in CSV data
     *
     * @param value The string to escape
     * @return The escaped string
     */
    private static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        // Check if value needs escaping
        boolean needsEscaping = value.contains("\"") || value.contains(",") || value.contains("\n");

        if (needsEscaping) {
            // Replace double quotes with two double quotes and wrap in quotes
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}