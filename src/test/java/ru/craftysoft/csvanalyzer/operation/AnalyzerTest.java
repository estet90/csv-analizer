package ru.craftysoft.csvanalyzer.operation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class AnalyzerTest {

    private final String resultFilePath = "src/test/resources/result.csv";

    @AfterEach
    void terminate() throws IOException {
        Files.delete(Paths.get(resultFilePath));
    }

    @Test
    void process() throws IOException {
        new Analyzer().process("src/test/resources", resultFilePath);

        try (var stream = Files.lines(Paths.get(resultFilePath))) {
            var prices = stream.map(line -> line.split(";"))
                    .peek(fields -> {
                        assertThat(fields).hasSize(5);
                        assertThatCode(() -> Integer.parseInt(fields[0])).doesNotThrowAnyException();
                        assertThatCode(() -> Float.parseFloat(fields[4])).doesNotThrowAnyException();
                    })
                    .map(fields -> Float.parseFloat(fields[4]))
                    .collect(Collectors.toList());
            for (int i = 1; i < prices.size(); i++) {
                assertThat(prices.get(i)).isGreaterThanOrEqualTo(prices.get(i - 1));
            }
        }
    }

}
