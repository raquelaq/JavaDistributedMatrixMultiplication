package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CSVWriterUtility {

    public static void writeToCSV(String filePath, String[] headers, String[] data) {
        try (FileWriter writer = new FileWriter(filePath, true)) {
            if (Files.size(Paths.get(filePath)) == 0) {
                writer.append(String.join(",", headers)).append("\n");
            }
            writer.append(String.join(",", data)).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
