package ru.otus;

import ru.otus.generator.QuotesGenerator;
import ru.otus.generator.QuotesGeneratorProcess;

/**
 * @author Aleksandr Semykin
 */
public class GeneratorApp {
    public static void main(String[] args) {
        var generator = new QuotesGenerator(100, 50);
        new QuotesGeneratorProcess(1000, generator).start();
    }
}
