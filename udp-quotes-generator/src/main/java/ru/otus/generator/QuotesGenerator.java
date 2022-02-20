package ru.otus.generator;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.domain.Quote;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Aleksandr Semykin
 */
public class QuotesGenerator {

    private static final Logger log = LoggerFactory.getLogger(QuotesGenerator.class);

    private final HashMap<String, Quote> quotes = new HashMap<>();
    private final List<String> isins = new ArrayList<>();
    @Getter private final int quotesCount;
    private final int distinctIsinsCount;
    private final List<Integer> quotesCountByIndex;
    private final Random random = new Random();

    public QuotesGenerator(int quotesCount, int distinctIsinCount, List<Double> remainCountDistributionByIndex) {
        this.quotesCount = quotesCount;
        this.distinctIsinsCount = distinctIsinCount;
        quotesCountByIndex = remainCountDistributionByIndex.stream()
                .map(percent -> (int) (Math.round(percent * (quotesCount - distinctIsinCount))))
                .map(count -> ++count)
                .collect(Collectors.toList());
        initQuotes();
    }

    public List<Quote> generate() {
        Collections.shuffle(isins);
        List<String> distinctIsins = isins.stream()
                .limit(distinctIsinsCount)
                .collect(Collectors.toList());

        final var iterator = quotesCountByIndex.iterator();
        return distinctIsins.stream()
                .map(isin -> generate(isin, iterator.hasNext() ? iterator.next() : 1))
                .flatMap(Collection::stream)
                .limit(quotesCount)
                .collect(Collectors.toList());
    }

    private List<Quote> generate(String isin, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> generateAndUpdate(isin))
                .collect(Collectors.toList());
    }

    private Quote generateAndUpdate(String isin) {
        Quote currentQuote = quotes.get(isin);
        BigDecimal newBid = incRandomlyAndGet(currentQuote.getBid());
        BigDecimal newAsk = incRandomlyAndGet(currentQuote.getAsk());
        Quote newQuote = new Quote(isin, newBid.min(newAsk), newBid.max(newAsk));
        quotes.put(isin, newQuote);
        return newQuote;
    }

    private BigDecimal incRandomlyAndGet(BigDecimal value) {
        return value
                .add(BigDecimal.valueOf(random.nextDouble(-5d, 5.01d)))
                .max(BigDecimal.valueOf(10))
                .min(BigDecimal.valueOf(1000))
                .setScale(2, RoundingMode.CEILING);
    }

    private void initQuotes() {
        var baseTemplate = "RU0000000000";
        for (int i = 0; i < quotesCount; i++) {
            var tail = String.valueOf(i);
            var template = baseTemplate.substring(0, baseTemplate.length() - tail.length());
            var isin = template + tail;
            isins.add(isin);
            quotes.put(isin, new Quote(isin, BigDecimal.valueOf(100), BigDecimal.valueOf(100)));
        }
    }
}
