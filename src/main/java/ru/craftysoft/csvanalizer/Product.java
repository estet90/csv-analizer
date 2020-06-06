package ru.craftysoft.csvanalizer;

public record Product(int id,
                      String name,
                      String condition,
                      String state,
                      float price) {
}
