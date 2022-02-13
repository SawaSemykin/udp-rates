package ru.otus.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.domain.Quote;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Aleksandr Semykin
 */
public class QuotesGenerator {

    private static final Logger log = LoggerFactory.getLogger(QuotesGenerator.class);

    private final HashMap<String, Quote> quotes = new HashMap<>();
    private final List<String> isins = new ArrayList<>();
    private final int quotesCount;
    private final int distinctIsinsCount;
    private final int remainCount;
    private final List<Double> remainCountRatioByIndex = List.of(.5, .3, .2); // 50% от remainCount пойдёт на котировки по 1ой бумаге, 30% - по 2ой, 20% - по 3ей
//    private final List<Double> remainCountRatioByIndex = List.of(1.d); // 100% от remainCount пойдут на котировки по 1ой бумаге
    private final List<Integer> quotesCountByIndex = new ArrayList<>();
    private final Random random = new Random();

    public QuotesGenerator(int quotesCount, int distinctIsinCount) {
        this.quotesCount = quotesCount;
        this.distinctIsinsCount = distinctIsinCount;
        remainCount = quotesCount - distinctIsinCount;
        init();
    }

    public List<Quote> generate() {
        Collections.shuffle(isins);
        List<String> distinctIsins = isins.stream()
                .limit(distinctIsinsCount)
                .collect(Collectors.toList());

        var generated = new ArrayList<Quote>();
        for (int i = 0; i < distinctIsins.size(); i++) {
            for (int j = 0; j < quotesCountByIndex.get(i); j++) {
                Quote currentQuote = quotes.get(distinctIsins.get(i));
                BigDecimal newBid = currentQuote.getBid()
                        .add(BigDecimal.valueOf(random.nextDouble(-5d, 5.01d)))
                        .min(BigDecimal.valueOf(10))
                        .max(BigDecimal.valueOf(1000))
                        .setScale(2, RoundingMode.CEILING);
                BigDecimal newAsk = currentQuote.getAsk()
                        .add(BigDecimal.valueOf(random.nextDouble(-5d, 5.01d)))
                        .min(BigDecimal.valueOf(10))
                        .max(BigDecimal.valueOf(1000))
                        .setScale(2, RoundingMode.CEILING);
                Quote newQuote = new Quote(distinctIsins.get(i), newBid.min(newAsk), newBid.max(newAsk));
                quotes.put(distinctIsins.get(i), newQuote);
                generated.add(newQuote);
            }
        }
        return generated;
    }

    private void init() {
        initQuotes();
        initQuotesCountByIndex();
    }

    private void initQuotes() {
        var template = "RU00076616";
        for (int i = 0; i < quotesCount; i++) {
            var tail = i < 10 ? "0" + i : "" + i;
            var isin = template + tail;
            isins.add(isin);
            quotes.put(isin, new Quote(isin, BigDecimal.valueOf(100), BigDecimal.valueOf(100)));
        }
    }

    private void initQuotesCountByIndex() {
        for (int i = 0; i < distinctIsinsCount; i++) {
            quotesCountByIndex.add(1);
        }
        int currentCount = 0;
        for (int i = 0; i < remainCountRatioByIndex.size() && currentCount != remainCount; i++) {
            int c = (int) (remainCount * remainCountRatioByIndex.get(i)) != 0 ? (int) (remainCount * remainCountRatioByIndex.get(i)) : 1;
            if (currentCount + c > remainCount || i == remainCountRatioByIndex.size() - 1){
                quotesCountByIndex.set(i, remainCount - currentCount + 1);
                currentCount = remainCount;
            } else {
                quotesCountByIndex.set(i, c + 1);
                currentCount += c;
           }
        }
        int totalCount = quotesCountByIndex.stream().mapToInt(Integer::intValue).sum();
        assert quotesCount == totalCount;
    }
}
