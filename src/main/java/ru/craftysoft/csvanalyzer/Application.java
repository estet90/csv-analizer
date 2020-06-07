package ru.craftysoft.csvanalyzer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import ru.craftysoft.csvanalyzer.operation.Analyzer;
import ru.craftysoft.csvanalyzer.operation.TestDataGenerator;

import java.util.ArrayList;

public class Application {

    private static final String analyzeMode = "analyze";
    private static final String generateMode = "generate";

    public static void main(String[] args) {
        if (args.length == 0 || "--help".equals(args[0])) {
            var helpText = """
                    Возможные ключи запуска
                    --mode - необязательный - режим работы приложения. Возможные значения 'analyze', 'generate'. По умолчанию используется 'analyze'.
                    --out - обязательный - для режима 'analyze' - директория, в которую записывается результат вычислений .
                                           для режима 'generate' - директория, в которую записываются сгенерированные файлы.
                    --in - обязательный для 'analyze' - директория, содержащая файлы для анализа.
                    --filesCount - необязательный - только для режима 'generate'. Количество генерируемых файлов. Должен быть целым положительным числом.
                                                    По умолчания 200.
                    --rowsCount - необязательный - только для режима 'generate'. Количество строк в генерируемом файле. Должен быть целым положительным числом.
                                                   По умолчания 50000.""";
            System.out.println(helpText);
            return;
        }
        var options = prepareOptions();
        try {
            var parsed = new DefaultParser().parse(options, args);
            var mode = parsed.getOptionValue("mode", analyzeMode);
            switch (mode) {
                case analyzeMode -> analyze(parsed);
                case generateMode -> generate(parsed);
                default -> throw new RuntimeException(String.format("Допустимые значения параметра mode '%s', '%s'", analyzeMode, generateMode));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Options prepareOptions() {
        var options = new Options();
        options.addOption("mode", "mode", true, "Тип выполняемой операции");
        options.addOption("in", "in", true, "Директория, содержащая файлы для анализа");
        options.addOption("out", "out", true, "Директория, в которую записывается результат");
        options.addOption("filesCount", "filesCount", true, "Количество генерируемых файлов");
        options.addOption("rowsCount", "rowsCount", true, "Количество строк в генерируемом файле");
        return options;
    }

    private static void generate(CommandLine parsed) {
        var errors = new ArrayList<String>();
        var out = parsed.getOptionValue("out");
        if (out == null) {
            errors.add("Не указана директория для генерации файлов");
        }
        var filesCountArgument = parsed.getOptionValue("filesCount", "200");
        int filesCount = 0;
        try {
            filesCount = Integer.parseInt(filesCountArgument);
            if (filesCount <= 0) {
                errors.add("Параметр 'filesCount' должен быть положительным");
            }
        } catch (NumberFormatException e) {
            errors.add(e.getMessage());
        }
        var rowsCountArgument = parsed.getOptionValue("rowsCount", "50000");
        int rowsCount = 0;
        try {
            rowsCount = Integer.parseInt(rowsCountArgument);
            if (rowsCount <= 0) {
                errors.add("Параметр 'rowsCount' должен быть положительным");
            }
        } catch (NumberFormatException e) {
            errors.add(e.getMessage());
        }
        if (!errors.isEmpty()) {
            throw new RuntimeException(errors.toString());
        }
        new TestDataGenerator().process(out, filesCount, rowsCount);
    }

    private static void analyze(CommandLine parsed) {
        var errors = new ArrayList<String>();
        var in = parsed.getOptionValue("in");
        if (in == null) {
            errors.add("Не указана директория с файлами для анализа");
        }
        var out = parsed.getOptionValue("out");
        if (out == null) {
            errors.add("Не указана директория для вывода результата");
        }
        if (!errors.isEmpty()) {
            throw new RuntimeException(errors.toString());
        }
        new Analyzer().process(in, out);
    }

}
