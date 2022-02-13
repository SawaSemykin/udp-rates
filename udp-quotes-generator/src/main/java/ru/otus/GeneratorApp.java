package ru.otus;

import ru.otus.generator.QuotesGeneratorProcess;
import ru.otus.generator.Process;

/**
 * @author Aleksandr Semykin
 */
public class GeneratorApp {
    public static void main(String[] args) {
        Process process = new QuotesGeneratorProcess();
        new Thread(process::start).start();
//        process.stop();
    }
}
