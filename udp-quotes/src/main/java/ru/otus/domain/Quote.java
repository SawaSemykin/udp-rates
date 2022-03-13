package ru.otus.domain;



import lombok.Value;

import java.math.BigDecimal;

/**
 * @author Aleksandr Semykin
 */
@Value
public class Quote {
    String isin;
    BigDecimal bid;
    BigDecimal ask;
}
