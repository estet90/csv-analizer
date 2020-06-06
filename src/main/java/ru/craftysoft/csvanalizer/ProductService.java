package ru.craftysoft.csvanalizer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

public class ProductService {

    private final TreeSet<Product> products = new TreeSet<>((o1, o2) -> {
        var compareDateResult = Float.compare(o1.price(), o2.price());
        return 0 != compareDateResult
                ? compareDateResult
                : Integer.compare(o1.hashCode(), o2.hashCode());
    });
    private final Map<Integer, TreeMap<Float, Product>> productsByIdGrouping = new HashMap<>();

    void addData(String path) {
        recursiveFileRead(new File(path), this::processFile);
        writeResult();
    }

    private void processFile(File file) {
        var start = System.currentTimeMillis();
        try (var bufferedReader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            for (String line; (line = bufferedReader.readLine()) != null; ) {
                var fields = line.split(";");
                var product = new Product(
                        Integer.parseInt(fields[0]),
                        fields[1],
                        fields[2],
                        fields[3],
                        Float.parseFloat(fields[4])
                );
                if (products.contains(product)) {
                    continue;
                }
                var productsLimit = 1000;
                if (products.size() == productsLimit) {
                    var price = product.price();
                    var last = products.last();
                    if (price >= last.price()) {
                        continue;
                    }
                    var first = products.first();
                    var id = product.id();
                    if (price <= first.price()) {
                        var map = productsByIdGrouping.get(id);
                        if (nonNull(map)) {
                            if (map.size() == 20) {
                                var lastEntry = map.pollLastEntry();
                                products.remove(lastEntry.getValue());
                            } else {
                                ofNullable(products.pollLast())
                                        .ifPresent(removedProduct -> ofNullable(productsByIdGrouping.get(removedProduct.id()))
                                                .ifPresent(treeMap -> treeMap.remove(removedProduct.price()))
                                        );
                            }
                            map.put(price, product);
                        } else {
                            ofNullable(products.pollLast())
                                    .ifPresent(removedProduct -> ofNullable(productsByIdGrouping.get(removedProduct.id()))
                                            .ifPresent(treeMap -> treeMap.remove(removedProduct.price()))
                                    );
                            productsByIdGrouping.put(id, new TreeMap<>(Map.of(price, product)));
                        }
                        products.add(product);
                    } else {
                        var map = productsByIdGrouping.get(id);
                        if (nonNull(map) && !(map.size() == 20 && price >= map.lastKey())) {
                            for (var entry : map.entrySet()) {
                                if (entry.getKey() > price) {
                                    if (map.size() == 20) {
                                        var lastEntry = map.pollLastEntry();
                                        products.remove(lastEntry.getValue());
                                    } else {
                                        ofNullable(products.pollLast())
                                                .ifPresent(removedProduct -> ofNullable(productsByIdGrouping.get(removedProduct.id()))
                                                        .ifPresent(treeMap -> treeMap.remove(removedProduct.price()))
                                                );
                                    }
                                    var size = map.size();
                                    map.put(price, product);
                                    if (size < map.size()) {
                                        products.add(product);
                                    }
                                    break;
                                }
                            }
                        } else {
                            ofNullable(products.pollLast())
                                    .ifPresent(removedProduct -> ofNullable(productsByIdGrouping.get(removedProduct.id()))
                                            .ifPresent(treeMap -> treeMap.remove(removedProduct.price()))
                                    );
                            productsByIdGrouping.put(id, new TreeMap<>(Map.of(price, product)));
                            products.add(product);
                        }
                    }
                } else if (products.size() == 0) {
                    products.add(product);
                    productsByIdGrouping.put(product.id(), new TreeMap<>(Map.of(product.price(), product)));
                } else {
                    var price = product.price();
                    var last = products.last();
                    var id = product.id();
                    if (price >= last.price()) {
                        var map = productsByIdGrouping.get(id);
                        if (nonNull(map)) {
                            if (map.size() < 20) {
                                products.add(product);
                                map.put(price, product);
                            }
                        } else {
                            products.add(product);
                            productsByIdGrouping.put(id, new TreeMap<>(Map.of(price, product)));
                        }
                        continue;
                    }
                    var first = products.first();
                    if (price <= first.price()) {
                        var map = productsByIdGrouping.get(id);
                        if (nonNull(map)) {
                            if (map.size() == 20) {
                                var lastEntry = map.pollLastEntry();
                                products.remove(lastEntry.getValue());
                            }
                            var size = map.size();
                            map.put(price, product);
                            if (size < map.size()) {
                                products.add(product);
                            }
                        } else {
                            productsByIdGrouping.put(id, new TreeMap<>(Map.of(price, product)));
                            products.add(product);
                        }
                    } else {
                        var map = productsByIdGrouping.get(id);
                        if (nonNull(map) && !(map.size() == 20 && price >= map.lastKey())) {
                            for (var entry : map.entrySet()) {
                                if (entry.getKey() > price) {
                                    if (map.size() == 20) {
                                        var lastEntry = map.pollLastEntry();
                                        products.remove(lastEntry.getValue());
                                    }
                                    var size = map.size();
                                    map.put(price, product);
                                    if (size < map.size()) {
                                        products.add(product);
                                    }
                                    break;
                                }
                            }
                        } else {
                            productsByIdGrouping.put(id, new TreeMap<>(Map.of(price, product)));
                            products.add(product);
                        }
                    }
                }
            }
            System.out.println(System.currentTimeMillis() - start + " " + products.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void writeResult() {

    }

    private void recursiveFileRead(File file, Consumer<File> processor) {
        if (file.isDirectory() && file.listFiles() != null) {
            for (var fileInDirectory : file.listFiles()) {
                recursiveFileRead(fileInDirectory, processor);
            }
        } else {
            processor.accept(file);
            System.out.println(file.getName());
        }
    }

}
