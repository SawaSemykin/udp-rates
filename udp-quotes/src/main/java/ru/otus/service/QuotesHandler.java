package ru.otus.service;

import ru.otus.domain.Elvl;
import ru.otus.domain.Quote;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;

public interface QuotesHandler {
    int POOL_SIZE = Runtime.getRuntime().availableProcessors();
    BigDecimal UNHANDLED_ELVL_VALUE = BigDecimal.valueOf(-1).setScale(2, RoundingMode.CEILING);

    void handle(List<Quote> quotes);
    void pushElvls();
    Collection<Elvl> getElvls();
}
