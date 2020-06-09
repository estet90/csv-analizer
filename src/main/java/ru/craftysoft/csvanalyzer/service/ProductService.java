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
    private final int maxNumberOfProductsWithTheSameId = 20;

    public TreeSet<Product> getProducts() {
        return products;
    }

    public synchronized void processProduct(Product product) {
        if (products.contains(product)) {
            return;
        }
        var productsLimit = 1000;
        if (products.size() == productsLimit) {
            handleProductWhenProductsHasMaxSize(product);
        } else if (products.size() == 0) {
            products.add(product);
            productsByIdGrouping.put(product.id(), new TreeMap<>(Map.of(product.price(), product)));
        } else {
            handleProduct(product);
        }
    }

    private void handleProductWhenProductsHasMaxSize(Product product) {
        var price = product.price();
        var lastProduct = products.last();
        if (price >= lastProduct.price()) {
            return;
        }
        var firstProduct = products.first();
        if (price <= firstProduct.price()) {
            addProductToStartWhenProductsHasMaxSize(product);
        } else {
            addProductWhenProductsHasMaxSize(product);
        }
    }

    private void addProductToStartWhenProductsHasMaxSize(Product product) {
        var id = product.id();
        var price = product.price();
        var map = productsByIdGrouping.get(id);
        if (nonNull(map)) {
            if (map.size() == maxNumberOfProductsWithTheSameId) {
                var lastEntry = map.pollLastEntry();
                products.remove(lastEntry.getValue());
            } else {
                pollLastProduct();
            }
            map.put(price, product);
        } else {
            pollLastProduct();
            productsByIdGrouping.put(id, new TreeMap<>(Map.of(price, product)));
        }
        products.add(product);
    }

    private void addProductWhenProductsHasMaxSize(Product product) {
        var id = product.id();
        var price = product.price();
        var map = productsByIdGrouping.get(id);
        if (nonNull(map) && !(map.size() == maxNumberOfProductsWithTheSameId && price >= map.lastKey())) {
            for (var entry : map.entrySet()) {
                if (entry.getKey() > price) {
                    if (map.size() == maxNumberOfProductsWithTheSameId) {
                        var lastEntry = map.pollLastEntry();
                        products.remove(lastEntry.getValue());
                    } else {
                        pollLastProduct();
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
            pollLastProduct();
            productsByIdGrouping.put(id, new TreeMap<>(Map.of(price, product)));
            products.add(product);
        }
    }

    private void pollLastProduct() {
        ofNullable(products.pollLast())
                .ifPresent(removedProduct -> ofNullable(productsByIdGrouping.get(removedProduct.id()))
                        .ifPresent(treeMap -> treeMap.remove(removedProduct.price()))
                );
    }

    private void handleProduct(Product product) {
        var price = product.price();
        var last = products.last();
        if (price >= last.price()) {
            addProductToEnd(product);
            return;
        }
        var first = products.first();
        if (price <= first.price()) {
            addProductToStart(product);
        } else {
            addProduct(product);
        }
    }

    private void addProduct(Product product) {
        var price = product.price();
        var id = product.id();
        var map = productsByIdGrouping.get(id);
        if (nonNull(map) && !(map.size() == maxNumberOfProductsWithTheSameId && price >= map.lastKey())) {
            for (var entry : map.entrySet()) {
                if (entry.getKey() > price) {
                    if (map.size() == maxNumberOfProductsWithTheSameId) {
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

    private void addProductToStart(Product product) {
        var price = product.price();
        var id = product.id();
        var map = productsByIdGrouping.get(id);
        if (nonNull(map)) {
            if (map.size() == maxNumberOfProductsWithTheSameId) {
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
    }

    private void addProductToEnd(Product product) {
        var price = product.price();
        var id = product.id();
        var map = productsByIdGrouping.get(id);
        if (nonNull(map)) {
            if (map.size() < maxNumberOfProductsWithTheSameId) {
                products.add(product);
                map.put(price, product);
            }
        } else {
            products.add(product);
            productsByIdGrouping.put(id, new TreeMap<>(Map.of(price, product)));
        }
    }


}
