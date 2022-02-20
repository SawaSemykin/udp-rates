package ru.otus;

import ru.otus.generator.QuotesGenerator;
import ru.otus.generator.QuotesGeneratorProcess;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Aleksandr Semykin
 */
public class GeneratorApp {
    public static void main(String[] args) throws IOException {
        try (InputStream input = GeneratorApp.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties props = new Properties();
            props.load(input);
            int destPort = Integer.parseInt(props.getProperty("generator.destination-port"));
            int quotesCount = Integer.parseInt(props.getProperty("generator.quotes.count"));
            int isinsCount = Integer.parseInt(props.getProperty("generator.quotes.distinct-isins-count"));
            int sendingRateInMillis = Integer.parseInt(props.getProperty("generator.quotes.sending-rate-in-millis"));
            List<Double> remainCountDistribution = Arrays.stream(props.getProperty("generator.quotes.remain-count-distribution").split(","))
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());


            var generator = new QuotesGenerator(quotesCount, isinsCount, remainCountDistribution);
            new QuotesGeneratorProcess(destPort, sendingRateInMillis, generator).start();
        }
    }
}
