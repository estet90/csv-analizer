package ru.craftysoft.csvanalyzer.service;

import ru.craftysoft.csvanalyzer.dto.Product;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

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

    public TreeSet<Product> getProducts() {
        return products;
    }

    //возможно этот метод стоит разделить на несколько методов поменьше. но я не хочу иметь много методов, меняющих глобальное состояние.
    public synchronized void processProduct(Product product) {
        if (products.contains(product)) {
            return;
        }
        var productsLimit = 1000;
        if (products.size() == productsLimit) {
            var price = product.price();
            var lastProduct = products.last();
            if (price >= lastProduct.price()) {
                return;
            }
            var firstProduct = products.first();
            var id = product.id();
            if (price <= firstProduct.price()) {
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
                return;
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


}
