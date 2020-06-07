package ru.craftysoft.csvanalyzer.dto;

public record Product(int id,
                      String name,
                      String condition,
                      String state,
                      float price) {
}
