package ru.craftysoft.csvanalyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class TestDataGenerator {

    void process(String folder, int filesCount, int rowsCount) {
        var lineSeparator = System.lineSeparator();
        for (int i = 1; i < filesCount + 1; i++) {
            var file = new File(folder + "/test" + i + ".csv");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                var current = ThreadLocalRandom.current();
                for (int j = 0; j < rowsCount; j++) {
                    var price = current.nextFloat() * 100;
                    var line = String.join(
                            ";",
                            String.valueOf(current.nextInt(1, 101)), "name", "condition", "state", String.valueOf(price)
                    );
                    line += lineSeparator;
                    writer.write(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
