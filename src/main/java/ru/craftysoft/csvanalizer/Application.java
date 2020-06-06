package ru.craftysoft.csvanalizer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class Application {

//    public static void main(String[] args) {
//        var product1 = new Product(1, "test", "test", "test", 1.0f);
//        var product2 = new Product(2, "test", "test", "test", 1.0f);
//        var set = new TreeSet<Product>(new Comparator<Product>() {
//            @Override
//            public int compare(Product o1, Product o2) {
//                var compareDateResult = Float.compare(o1.price(), o2.price());
//                return 0 != compareDateResult
//                        ? compareDateResult
//                        : Integer.compare(o1.hashCode(), o2.hashCode());
//            }
//        });
//        set.add(product1);
//        set.add(product2);
//        System.out.println(set);
//        new ProductService().addData();
//    }

    public static void main(String[] args) {
        new ProductService().addData("/home/dkononov/git/samples/csv-analizer/src/main/resources");
    }

    public static void generate() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/fileName.csv"))) {
            var current = ThreadLocalRandom.current();
            for (int i = 0; i < 10000; i++) {
                var line = String.join(
                        ";",
                        String.valueOf(current.nextInt(1, 101)), "name", "condition", "state", String.valueOf(current.nextFloat())
                );
                line += "\n";
                writer.write(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
