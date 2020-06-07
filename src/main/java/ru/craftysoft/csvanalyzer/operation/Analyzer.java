package ru.craftysoft.csvanalyzer.operation;

import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.craftysoft.csvanalyzer.dto.Product;
import ru.craftysoft.csvanalyzer.service.ProductService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Analyzer {

    private final ProductService productService = new ProductService();

    private static final Logger logger = LogManager.getLogger(ProductService.class);

    public void process(String in, String out) {
        var start = System.currentTimeMillis();
        var point = "FileProcessor.process";
        logger.info("{}.in", point);
        var threadsCount = Runtime.getRuntime().availableProcessors();
        var executor = Executors.newFixedThreadPool(threadsCount);
        try {
            var processors = new ArrayList<Callable<Object>>();
            recursiveFileProcess(processors, new File(in), this::processFile);
            var futures = executor.invokeAll(processors);
            for (var future : futures) {
                future.get();
            }
            executor.shutdown();
            writeResult(out);
            var processTime = System.currentTimeMillis() - start;
            logger.info("{}.out time={}ms", point, processTime);
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
                var processFileTime = System.currentTimeMillis() - start;
                logger.info("{}.out time={}ms", point, processFileTime);
            } catch (Exception e) {
                logger.error("{}.thrown", point, e);
            }
        }
    }

    private void recursiveFileProcess(List<Callable<Object>> processors, File file, Consumer<File> processor) {
        if (file.isDirectory() && file.listFiles() != null) {
            for (var fileInDirectory : file.listFiles()) { //idea предлагает тут сделать проверку на NULL, но у меня она делается выше
                recursiveFileProcess(processors, fileInDirectory, processor);
            }
        } else {
            processors.add(() -> {
                processor.accept(file);
                return null;
            });
        }
    }

    private void writeResult(String out) throws IOException {
        var lineSeparator = System.lineSeparator();
        var file = new File(out);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            var products = productService.getProducts();
            for (var product : products) {
                var price = BigDecimal.valueOf(product.price()).setScale(2, RoundingMode.HALF_DOWN);
                var line = String.join(
                        ";",
                        String.valueOf(product.id()), product.name(), product.condition(), product.state(), price.toString()
                );
                line += lineSeparator;
                writer.write(line);
            }
        }
    }
}
