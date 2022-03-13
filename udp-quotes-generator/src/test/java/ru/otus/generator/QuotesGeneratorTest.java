package ru.otus.generator;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import ru.otus.domain.Quote;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class QuotesGeneratorTest {


    @Test
    void generateDistinctQuotesTest() {
        QuotesGenerator gen = new QuotesGenerator(100, 100, Collections.emptyList());

        List<Quote> quotes  = gen.generate();

        assertThat(quotes).map(Quote::getIsin)
                .hasSize(100)
                .doesNotHaveDuplicates();
    }

    @Test
    void generateQuotesOfTheSameIsinTest() {
        QuotesGenerator gen = new QuotesGenerator(100, 1, List.of(1.0d));

        List<Quote> quotes = gen.generate();
        var isin = quotes.get(0).getIsin();

        assertThat(quotes).map(Quote::getIsin)
                .hasSize(100)
                .allMatch(isin::equals);
    }

    @Test
    void generateRemainCountDistributionTest() {
        QuotesGenerator gen = new QuotesGenerator(100, 50, List.of(.5d, .3d, .2d));

        List<Quote> quotes = gen.generate();
        var first = new Condition<String>(quotes.get(0).getIsin()::equals, "the1stIsin");
        assertThat(quotes).map(Quote::getIsin).hasSize(100).haveExactly(26, first);


        quotes = quotes.stream().skip(26).collect(Collectors.toList());
        var second = new Condition<String>(quotes.get(0).getIsin()::equals, "the2ndIsin");
        assertThat(quotes).map(Quote::getIsin).haveExactly(16, second);

        quotes = quotes.stream().skip(16).collect(Collectors.toList());
        var third = new Condition<String>(quotes.get(0).getIsin()::equals, "the3rdIsin");
        assertThat(quotes).map(Quote::getIsin).haveExactly(11, third);
    }
}