package ru.craftysoft.csvanalyzer;

import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Analizer {

    private final ProductService productService = new ProductService();

    private static final Logger logger = LogManager.getLogger(ProductService.class);

    void process(String in, String out) {
        var point = "FileProcessor.process";
        logger.info("{}.in", point);
        var threadsCount = Runtime.getRuntime().availableProcessors();
        var executor = Executors.newFixedThreadPool(threadsCount);
        try {
            recursiveFileProcess(executor, new File(in), this::processFile);
            executor.shutdown();
            while (!executor.isTerminated()) {
                Thread.sleep(1000);
            }
            writeResult(out);
            logger.info("{}.out", point);
        } catch (Exception e) {
            logger.error("{}.thrown", point, e);
        }
    }

    private void processFile(File file) {
        var start = System.currentTimeMillis();
        try (var ignored = CloseableThreadContext.put("traceId", UUID.randomUUID().toString())
                .put("file", file.getName())) {
            var point = "FileProcessor.processFile";
            logger.info("{}.in", point);
            try (var bufferedReader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                for (String line; (line = bufferedReader.readLine()) != null; ) {
                    try {
                        var fields = line.split(";");
                        var product = new Product(
                                Integer.parseInt(fields[0]),
                                fields[1],
                                fields[2],
                                fields[3],
                                Float.parseFloat(fields[4])
                        );
                        productService.processProduct(product);
                    } catch (Exception e) {
                        logger.error("{}.thrown line={}", point, line, e);
                    }
                }
                var workTime = System.currentTimeMillis() - start;
                logger.info("{}.out time={}ms", point, workTime);
            } catch (Exception e) {
                logger.error("{}.thrown", point, e);
            }
        }
    }

    private void recursiveFileProcess(ExecutorService executor, File file, Consumer<File> processor) {
        if (file.isDirectory() && file.listFiles() != null) {
            for (var fileInDirectory : file.listFiles()) { //idea предлагает тут сделать проверку на NULL, но у меня она делается выше
                recursiveFileProcess(executor, fileInDirectory, processor);
            }
        } else {
            executor.execute(() -> processor.accept(file));
        }
    }

    private void writeResult(String out) throws IOException {
        var lineSeparator = System.lineSeparator();
        var file = new File(out);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            var products = productService.getProducts();
            for (var product : products) {
                var line = String.join(
                        ";",
                        String.valueOf(product.id()), product.name(), product.condition(), product.state(), String.valueOf(product.price())
                );
                line += lineSeparator;
                writer.write(line);
            }
        }
    }
}
