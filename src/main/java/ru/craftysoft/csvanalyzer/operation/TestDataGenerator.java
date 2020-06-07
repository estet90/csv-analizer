package ru.craftysoft.csvanalyzer.operation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.ThreadLocalRandom;

public class TestDataGenerator {

    private static final Logger logger = LogManager.getLogger(TestDataGenerator.class);

    public void process(String folder, int filesCount, int rowsCount) {
        logger.info("TestDataGenerator.process.in");
        try {
            var lineSeparator = System.lineSeparator();
            for (int i = 1; i < filesCount + 1; i++) {
                var directory = new File(folder);
                if (!directory.exists()) {
                    var created = directory.mkdir();
                    if (created) {
                        logger.info("TestDataGenerator.process new directory");
                    } else {
                        throw new IllegalArgumentException(String.format("Не удалось создать папку '%s'", folder));
                    }
                }
                var file = new File(folder + "/test" + i + ".csv");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    var current = ThreadLocalRandom.current();
                    for (int j = 0; j < rowsCount; j++) {
                        var price = current.nextInt(100, 1001) + "." + current.nextInt(1, 100);
                        var line = String.join(
                                ";",
                                String.valueOf(current.nextInt(1, 101)), "name", "condition", "state", price
                        );
                        line += lineSeparator;
                        writer.write(line);
                    }
                }
            }
            logger.info("TestDataGenerator.process.out directory={} filesCount={}", folder, filesCount);
        } catch (Exception e) {
            logger.error("TestDataGenerator.process.thrown", e);
        }
    }

}
